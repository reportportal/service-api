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
                        resourceRequestCpu: '250m',
                        resourceLimitCpu: '800m',
                        resourceRequestMemory: '1024Mi',
                        resourceLimitMemory: '2048Mi'),
                containerTemplate(name: 'kubectl', image: 'lachlanevenson/k8s-kubectl:v1.8.8', command: 'cat', ttyEnabled: true),
                containerTemplate(name: 'helm', image: 'lachlanevenson/k8s-helm:v3.0.2', command: 'cat', ttyEnabled: true),
                containerTemplate(name: 'httpie', image: 'blacktop/httpie', command: 'cat', ttyEnabled: true),
                containerTemplate(name: 'jre', image: 'openjdk:8-jre-alpine', command: 'cat', ttyEnabled: true),
                containerTemplate(name: 'jdk', image: 'openjdk:8-jdk-alpine', command: 'cat', ttyEnabled: true)
        ],
        imagePullSecrets: ["regcred"],
        volumes: [
                hostPathVolume(hostPath: '/var/run/docker.sock', mountPath: '/var/run/docker.sock'),
                secretVolume(mountPath: '/etc/.dockercreds', secretName: 'docker-creds'),
                secretVolume(mountPath: '/etc/.sealights-token', secretName: 'sealights-token'),
                hostPathVolume(mountPath: '/root/.m2/repository', hostPath: '/tmp/jenkins/.m2/repository')
        ]
) {

    node("${label}") {

        def sealightsTokenPath = "/etc/.sealights-token/token"
        def srvRepo = "quay.io/reportportal/service-api"
        def sealightsAgentUrl = "https://agents.sealights.co/sealights-java/sealights-java-latest.zip"
        def sealightsAgentArchive = sealightsAgentUrl.substring(sealightsAgentUrl.lastIndexOf('/') + 1)

        def k8sDir = "kubernetes"
        def ciDir = "reportportal-ci"
        def appDir = "app"
        def testDir = "tests"
        def serviceName = "service-api"
        def k8sNs = "reportportal"
        def sealightsDir = 'sealights'

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
                    git branch: branchToBuild, url: 'https://github.com/reportportal/service-api.git'
                }
            }
        }, 'Checkout tests': {
            stage('Checkout tests') {
                dir(testDir) {
                    git url: 'git@git.epam.com:EPM-RPP/tests.git', branch: "develop", credentialsId: 'epm-gitlab-key'
                }
            }
        }, 'Download Sealights': {
            stage('Download Sealights'){
                dir(sealightsDir) {
                    sh "wget ${sealightsAgentUrl}"
                    unzip sealightsAgentArchive
                }
            }
        }

        def utils = load "${ciDir}/jenkins/scripts/util.groovy"
        def helm = load "${ciDir}/jenkins/scripts/helm.groovy"
        def docker = load "${ciDir}/jenkins/scripts/docker.groovy"

        docker.init()
        helm.init()
        utils.scheduleRepoPoll()

        def snapshotVersion = utils.readProperty("app/gradle.properties", "version")
        def buildVersion = "BUILD-${env.BUILD_NUMBER}"
        def srvVersion = "${snapshotVersion}-${buildVersion}"
        def tag = "$srvRepo:$srvVersion"

        def sealightsToken = utils.execStdout("cat $sealightsTokenPath")
        def sealightsSession;
        stage ('Init Sealights') {
            dir(sealightsDir) {
                container ('jre') {
                    sh "java -jar sl-build-scanner.jar -config -tokenfile $sealightsTokenPath -appname service-api -branchname $branchToBuild -buildname $srvVersion -pi '*com.epam.ta.reportportal.*'"
                    sealightsSession = utils.execStdout("cat buildSessionId.txt")
                }
            }
        }

        stage('Build Docker Image') {
            dir(appDir) {
                container('docker') {
                    sh "docker build -f docker/Dockerfile-develop --build-arg sealightsToken=$sealightsToken --build-arg sealightsSession=$sealightsSession --build-arg buildNumber=$buildVersion -t $tag ."
                    sh "docker push $tag"
                }
            }


        }

        def jvmArgs = params.get('JVM_ARGS')
        if(jvmArgs == null){
            jvmArgs = '-Xms2G -Xmx3g -DLOG_FILE=app.log -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/tmp'
        }
        def enableSealights = params.get('ENABLE_SEALIGHTS') != null && params.get('ENABLE_SEALIGHTS')
        if(enableSealights){
            jvmArgs = jvmArgs + ' -javaagent:./plugins/sl-test-listener.jar -Dsl.tokenFile=sealights-token.txt -Dsl.buildSessionIdFile=sealights-session.txt -Dsl.filesStorage=/tmp'
        }

        stage('Deploy to Dev Environment') {
            helm.deploy("$k8sDir/reportportal/v5", ["serviceapi.repository": srvRepo, "serviceapi.tag": srvVersion, "serviceapi.jvmArgs" : "\"$jvmArgs\""], false) // without wait
        }

        stage('Execute DVT Tests') {
            helm.testDeployment("reportportal", "reportportal-api", "$srvVersion")
        }

        def testEnv = 'gcp'
        def sealightsTokenFile = "sl-token.txt"
        def sealightsSessionFile = "buildsession.txt"
        try {
            stage('Integration tests') {
                dir("${testDir}") {
                    container('jdk') {
                        echo "Running RP integration tests on env: ${testEnv}"
                        writeFile(file: sealightsSessionFile, text: sealightsSession, encoding: "UTF-8")
                        writeFile(file: sealightsTokenFile, text: sealightsToken, encoding: "UTF-8")
                        sh "echo 'rp.attributes=v5:${testEnv};' >> ${serviceName}/src/test/resources/reportportal.properties"
                        sh "./gradlew :${serviceName}:test -Denv=${testEnv} -Psl.tokenFile=${sealightsTokenFile} -Psl.buildSessionIdFile=${sealightsSessionFile}"
                    }
                }
            }
        } finally {
            dir("${testDir}/${serviceName}") {
                junit 'build/test-results/test/*.xml'
            }
        }
    }
}
