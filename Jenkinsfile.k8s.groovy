#!groovy

//String podTemplateConcat = "${serviceName}-${buildNumber}-${uuid}"
def label = "worker-${UUID.randomUUID().toString()}"
println("label")
println("${label}")

podTemplate(
        label: "${label}",
        containers: [
                containerTemplate(name: 'jnlp', image: 'jenkins/jnlp-slave:alpine'),
                containerTemplate(name: 'docker', image: 'docker', command: 'cat', ttyEnabled: true,
                        resourceRequestCpu: '500m',
                        resourceLimitCpu: '800m',
                        resourceRequestMemory: '1024Mi',
                        resourceLimitMemory: '2048Mi'),
                containerTemplate(name: 'kubectl', image: 'lachlanevenson/k8s-kubectl:v1.8.8', command: 'cat', ttyEnabled: true),
                containerTemplate(name: 'helm', image: 'lachlanevenson/k8s-helm:latest', command: 'cat', ttyEnabled: true),
                containerTemplate(name: 'httpie', image: 'blacktop/httpie', command: 'cat', ttyEnabled: true),
                containerTemplate(name: 'maven', image: 'maven:3.6.1-jdk-8-alpine', command: 'cat', ttyEnabled: true,
                        resourceRequestCpu: '1000m',
                        resourceLimitCpu: '2000m',
                        resourceRequestMemory: '1024Mi',
                        resourceLimitMemory: '3072Mi'),
                containerTemplate(name: 'jre', image: 'openjdk:8-jre-alpine', command: 'cat', ttyEnabled: true)
        ],
        imagePullSecrets: ["regcred"],
        volumes: [
                hostPathVolume(hostPath: '/var/run/docker.sock', mountPath: '/var/run/docker.sock'),
                secretVolume(mountPath: '/etc/.dockercreds', secretName: 'docker-creds'),
                hostPathVolume(mountPath: '/root/.m2', hostPath: '/tmp/jenkins/.m2')
        ]
) {

    node("${label}") {

        def sealightsTokenPath = "/etc/.sealights-token/token"
        def srvRepo = "quay.io/reportportal/service-api"

        def k8sDir = "kubernetes"
        def ciDir = "reportportal-ci"
        def appDir = "app"
        def testDir = "tests"
        def k8sNs = "reportportal"

        def branchToBuild = params.get('COMMIT_HASH', 'develop')


        parallel 'Checkout Infra': {
            stage('Checkout Infra') {
                sh 'mkdir -p ~/.ssh'
                sh 'ssh-keyscan -t rsa github.com >> ~/.ssh/known_hosts'
                sh 'ssh-keyscan -t rsa git.epam.com >> ~/.ssh/known_hosts'
                dir(k8sDir) {
                    git branch: "master", url: 'https://github.com/reportportal/kubernetes.git'

                }
                dir(ciDir) {
                    git credentialsId: 'epm-gitlab-key', branch: "master", url: 'git@git.epam.com:epmc-tst/reportportal-ci.git'
                }

            }
        }, 'Checkout Service': {
            stage('Checkout Service') {
                dir(appDir) {
                    checkout([$class: 'GitSCM', branches: [[name: branchToBuild]],
                              browser: [$class: 'GithubWeb', repoUrl: 'https://github.com/reportportal/service-api/'],
                              doGenerateSubmoduleConfigurations: false, extensions: [], submoduleCfg: [],
                              userRemoteConfigs: [[url: 'https://github.com/reportportal/service-api.git']]])
                }
            }
        }, 'Checkout tests': {
            stage('Checkout tests') {
                dir(testDir) {
                    git url: 'git@git.epam.com:EPM-RPP/tests.git', branch: "dev-v5", credentialsId: 'epm-gitlab-key'
                }
            }
        }

        def test = load "${ciDir}/jenkins/scripts/test.groovy"
        def utils = load "${ciDir}/jenkins/scripts/util.groovy"
        def helm = load "${ciDir}/jenkins/scripts/helm.groovy"
        def docker = load "${ciDir}/jenkins/scripts/docker.groovy"

        docker.init()
        helm.init()
        utils.scheduleRepoPoll()

//        dir('app') {
//            try {
//                container('docker') {
//                    stage('Build App') {
//                        sh "gradle build --full-stacktrace"
//                    }
//                }
//            } finally {
//                junit 'build/test-results/**/*.xml'
//                dependencyCheckPublisher pattern: 'build/reports/dependency-check-report.xml'
//
//            }
//
//        }
        def snapshotVersion = utils.readProperty("app/gradle.properties", "version")
        def buildVersion = "BUILD-${env.BUILD_NUMBER}"
        def srvVersion = "${snapshotVersion}-${buildVersion}"
        def tag = "$srvRepo:$srvVersion"

        def sealightsSession;
        stage ('Init Sealights') {
            dir("$appDir/sealights") {
                container ('jre') {
                    sh "java -jar sl-build-scanner.jar -config -tokenfile $sealightsTokenPath -appname service-api -branchname $branchToBuild -buildname "$srvVersion" -pi '*com.epam.ta.reportportal.*'"
                    sealightsSession = utils.execStdout("cat buildSessionId.txt")
                }
            }

        }

        stage('Build Docker Image') {
            dir(appDir) {
                container('docker') {
                    sh "docker build -f docker/Dockerfile-develop --build-arg  --build-arg sealightsSession=$sealightsSession buildNumber=$buildVersion -t $tag ."
                    sh "docker push $tag"
                }
            }


        }
        stage('Deploy to Dev Environment') {
            container('helm') {
                dir("$k8sDir/reportportal/v5") {
                    sh 'helm dependency update'
                }
                sh "helm upgrade --reuse-values --set serviceapi.repository=$srvRepo --set serviceapi.tag=$srvVersion --wait -f ./$ciDir/rp/values-ci.yml reportportal ./$k8sDir/reportportal/v5"
            }
        }

        stage('Execute DVT Tests') {
            def srvUrl
            container('kubectl') {
                def srvName = utils.getServiceName(k8sNs, "api")
                srvUrl = utils.getServiceEndpoint(k8sNs, srvName)
            }
            if (srvUrl == null) {
                error("Unable to retrieve service URL")
            }
            container('httpie') {
                test.checkVersion("http://$srvUrl", "$srvVersion")
            }
        }

        try {
            stage('Integration tests') {
                def testEnv = 'gcp-k8s'
                dir(testDir) {
                    container('maven') {
                        echo "Running RP integration tests on env: ${testEnv}"
                        sh "mvn clean test -Denv=${testEnv}"
                    }
                }
            }
        } finally {
            dir(testDir) {
                junit 'target/surefire-reports/*.xml'
            }
        }

    }
}
