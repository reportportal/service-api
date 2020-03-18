#!groovy
@Library('commons') _


def setupJob(branch = 'develop', pollScm = "H/10 * * * *") {
    def buildParams = [
            string(name: 'COMMIT_HASH', defaultValue: branch, description: 'Commit Hash or branch name'),
            booleanParam(name: 'ENABLE_SEALIGHTS', defaultValue: false, description: 'Enable Sealights plugin'),
            string(name: 'JVM_ARGS', defaultValue: '-Xms2G -Xmx3g -DLOG_FILE=app.log -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/tmp',
                    description: 'JVM arguments which will be bypassed to command line. Do not put sealights arguments here.')
    ]
    properties([
            pipelineTriggers([
                    pollSCM(pollScm)
            ]),
            parameters(buildParams)
    ])
}

def String readSecretsDirectory(String dirName) {
    echo "Reading directory ${dirName}"
    String fileNames = sh(script: "ls -1 $dirName", returnStdout: true)
    String [] fileList = fileNames.split('\\n')
    return fileList.collect {
        it + '='+ sh(script: "cat $dirName/$it", returnStdout: true).trim()
    }
}


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
                containerTemplate(name: 'helm', image: 'lachlanevenson/k8s-helm:v3.1.1', command: 'cat', ttyEnabled: true),
                containerTemplate(name: 'httpie', image: 'blacktop/httpie', command: 'cat', ttyEnabled: true),
                containerTemplate(name: 'jre', image: 'openjdk:8-jre-alpine', command: 'cat', ttyEnabled: true),
                containerTemplate(name: 'jdk', image: 'openjdk:8-jdk-alpine', command: 'cat', ttyEnabled: true,
                        resourceRequestCpu: '800m',
                        resourceLimitCpu: '3000m',
                        resourceRequestMemory: '2867Mi',
                        resourceLimitMemory: '4096Mi')
        ],
        imagePullSecrets: ["regcred"],
        volumes: [
                hostPathVolume(hostPath: '/var/run/docker.sock', mountPath: '/var/run/docker.sock'),
                secretVolume(mountPath: '/etc/.dockercreds', secretName: 'docker-creds'),
                secretVolume(mountPath: '/etc/.sealights-token', secretName: 'sealights-token'),
                secretVolume(mountPath: '/etc/.test-secrets', secretName: 'test-secrets'),
                hostPathVolume(mountPath: '/root/.m2/repository', hostPath: '/tmp/jenkins/.m2/repository')
        ]
) {
    node("${label}") {

        // Set pipeline parameters and triggers
        setupJob('v5.1-stable')
        def testSecrets = readSecretsDirectory('/etc/.test-secrets')

        def sealightsTokenPath = "/etc/.sealights-token/token"
        def srvRepo = "quay.io/reportportal/service-api"
        def sealightsAgentUrl = "https://agents.sealights.co/sealights-java/sealights-java-latest.zip"
        def sealightsAgentArchive = sealightsAgentUrl.substring(sealightsAgentUrl.lastIndexOf('/') + 1)

        def k8sDir = "kubernetes"
        def appDir = "app"
        def testDir = "tests"
        def serviceName = "service-api"
        def sealightsDir = 'sealights'
        def branchToBuild = params.get('COMMIT_HASH')

        parallel 'Checkout Infra': {
            stage('Checkout Infra') {
                sh 'mkdir -p ~/.ssh'
                sh 'ssh-keyscan -t rsa github.com >> ~/.ssh/known_hosts'
                sh 'ssh-keyscan -t rsa git.epam.com >> ~/.ssh/known_hosts'
                dir(k8sDir) {
                    git branch: "master", url: 'https://github.com/reportportal/kubernetes.git'

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
                    git url: 'git@git.epam.com:EPM-RPP/tests.git', branch: "v5.1-stable", credentialsId: 'epm-gitlab-key'
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

        dockerUtil.init()
        helm.init()

        sast('reportportal_services_sast', 'rp/carrier/config.yaml', 'service-index', false)

        def snapshotVersion = util.readProperty("app/gradle.properties", "version")
        def buildVersion = "BUILD-${env.BUILD_NUMBER}"
        def srvVersion = "${snapshotVersion}-${buildVersion}"
        def tag = "$srvRepo:$srvVersion"
        def enableSealights = params.get('ENABLE_SEALIGHTS')

        def sealightsToken = util.execStdout("cat $sealightsTokenPath")
        def sealightsSession = "";
        stage ('Init Sealights') {
            if(enableSealights) {
                dir(sealightsDir) {
                    container('jre') {
                        echo "Generating Sealights build session ID"
                        sh "java -jar sl-build-scanner.jar -config -tokenfile $sealightsTokenPath -appname service-api -branchname $branchToBuild -buildname $srvVersion -pi '*com.epam.ta.reportportal.*'"
                        sealightsSession = util.execStdout("cat buildSessionId.txt")
                    }
                }
            } else {
                echo "Sealights is disabled. Skipping build session ID generation"
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
        if(jvmArgs == null || jvmArgs.trim().isEmpty()){
            jvmArgs = '-Xms2G -Xmx3g -DLOG_FILE=app.log -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/tmp'
        }

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
        def testPhase = "smoke"
        try {
            withEnv(testSecrets) {
                stage('Smoke tests') {
                    dir("${testDir}") {
                        container('jdk') {
                            echo "Running RP integration tests on env: ${testEnv}"
                            writeFile(file: sealightsTokenFile, text: sealightsToken, encoding: "UTF-8")
                            sh "echo 'rp.attributes=v5:${testEnv};' >> ${serviceName}/src/test/resources/reportportal.properties"
                            sh "./gradlew :${serviceName}:${testPhase} -Denv=${testEnv} -Psl.tokenFile=${sealightsTokenFile} -Psl.buildSessionId=${sealightsSession}"
                        }
                    }
                }
            }
        } finally {
            dir("${testDir}/${serviceName}") {
                junit "build/test-results/${testPhase}/*.xml"
            }
        }

        dast('reportportal_dast', 'rp/carrier/config.yaml', 'rpportal_dev_dast', false)
    }
}
