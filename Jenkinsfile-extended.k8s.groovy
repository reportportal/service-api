#!groovy
@Library('commons') _

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
                containerTemplate(name: 'gradle', image: 'gradle:5.2.1-jdk11', command: 'cat', ttyEnabled: true,
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
                secretVolume(mountPath: '/etc/.saucelabs-accesskey', secretName: 'saucelabs-accesskey'),
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
                    git url: 'git@git.epam.com:EPM-RPP/tests.git', branch: "v5.1-stable", credentialsId: 'epm-gitlab-key'
                }
            }
        }, 'Download Sealights': {
            stage('Download Sealights') {
                dir(sealightsDir) {
                    sh "wget ${sealightsAgentUrl}"
                    unzip sealightsAgentArchive
                }
            }
        }

        dockerUtil.init()
        helm.init()
        util.scheduleRepoPoll()

        def snapshotVersion = util.readProperty("app/gradle.properties", "version")
        def buildVersion = "BUILD-DEMO-${env.BUILD_NUMBER}"
        def srvVersion = "${snapshotVersion}-${buildVersion}"
        def tag = "$srvRepo:$srvVersion"
        def enableSealights = params.get('ENABLE_SEALIGHTS') != null && params.get('ENABLE_SEALIGHTS')

        def sealightsToken = util.execStdout("cat $sealightsTokenPath")
        def sealightsSession = "";
        stage('Init Sealights') {
            if (enableSealights) {
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

        // Add to the main CI pipelines SAST step:
        def sastJobName = 'reportportal_services_sast'
        stage('Run SAST') {
            catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
                println("Triggering build of SAST job: ${sastJobName}...")
                build job: sastJobName,
                        parameters: [
                                string(name: 'CONFIG', value: 'rp/carrier/config.yaml'),
                                string(name: 'SUITE', value: 'service-api'),
                                booleanParam(name: 'DEBUG', value: false)
                        ],
                        propagate: false, wait: false // true or false: Wait for job finish
            }
        }

        def jvmArgs = params.get('JVM_ARGS')
        if (jvmArgs == null || jvmArgs.trim().isEmpty()) {
            jvmArgs = '-Xms2G -Xmx3g -DLOG_FILE=app.log -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/tmp'
        }
        if (enableSealights) {
            jvmArgs = jvmArgs + ' -javaagent:./plugins/sl-test-listener.jar -Dsl.tokenFile=sealights-token.txt -Dsl.buildSessionIdFile=sealights-session.txt -Dsl.filesStorage=/tmp'
        }

        stage('Deploy to Dev Environment') {
            helm.deploy("$k8sDir/reportportal/v5", ["serviceapi.repository": srvRepo, "serviceapi.tag": srvVersion, "serviceapi.jvmArgs": "\"$jvmArgs\""], false)
            // without wait
        }

        stage('Execute DVT Tests') {
            helm.testDeployment("reportportal", "reportportal-api", "$srvVersion")
        }

        def testEnv = 'gcp'
        def sealightsTokenFile = "sl-token.txt"
        def testPhase = "smoke"
        stage('Smoke tests') {
            dir("${testDir}/${serviceName}") {
                container('gradle') {
                    try {
                        echo "Running RP integration tests on env: ${testEnv}"
                        writeFile(file: sealightsTokenFile, text: sealightsToken, encoding: "UTF-8")
                        sh "echo 'rp.attributes=v5:${testEnv};' >> src/test/resources/reportportal.properties"
                        sh "gradle :${serviceName}:${testPhase} -Denv=${testEnv} -Psl.tokenFile=${sealightsTokenFile} -Psl.buildSessionId=${sealightsSession}"
                    } finally {
                        junit "build/test-results/${testPhase}/*.xml"
                    }
                }
            }
        }

        parallel 'Regression tests': {
            stage('Regression tests') {
                dir("${testDir}/${serviceName}") {
                    container('gradle') {
                        try {
                            echo "Running RP integration tests on env: ${testEnv}"
                            writeFile(file: sealightsTokenFile, text: sealightsToken, encoding: "UTF-8")
                            sh "echo 'rp.attributes=v5:${testEnv};' >> src/test/resources/reportportal.properties"
                            sh "gradle :${serviceName}:regressionTesting -Denv=${testEnv} -Psl.tokenFile=${sealightsTokenFile} -Psl.buildSessionId=${sealightsSession}"
                        } finally {
                            junit "build/test-results/regressionTesting/*.xml"
                        }
                    }
                }
            }
        }, 'UI tests': {
            stage('UI tests') {
                dir("${testDir}/ui-tests") {
                    container('gradle') {
                        try {
                            echo "Run ui desktop tests on env: ${testEnv}"
                            if (!sealightsSession.empty) {
                                writeFile(file: 'buildsession.txt', text: sealightsSession, encoding: "UTF-8")
                                writeFile(file: sealightsTokenFile, text: sealightsToken, encoding: "UTF-8")
                            }
                            def browser = 'firefox'
                            def accessKey = util.execStdout 'cat /etc/.saucelabs-accesskey/accesskey'
                            def url = "https://avarabyeu:$accessKey@ondemand.eu-central-1.saucelabs.com:443/wd/hub"
                            sh """
                               gradle --build-cache :ui-tests:cucumber \
                                      -Dspring.profiles.active=desktop \
                                      -Dbrowser.remote=true \
                                      -Dbrowser.url=$url \
                                      -Dbrowser.name=$browser
                           """
                        } finally {
                            step([$class             : 'CucumberReportPublisher',
                                  jsonReportDirectory: 'ui-tests/reports',
                                  fileIncludePattern : '*.json'])
                        }
                    }
                }
            }
        }, 'Mobile tests': {
            stage('Execute Tests') {
                container('gradle') {
                    withEnv(['K8S=true']) {
                        dir("$testDir/ui-tests") {
                            try {
                                echo "Run ui mobile tests on env: ${testEnv}"
                                if (!sealightsSession.empty) {
                                    writeFile(file: 'buildsession.txt', text: sealightsSession, encoding: "UTF-8")
                                    writeFile(file: 'sl-token.txt', text: sealightsToken, encoding: "UTF-8")
                                }

                                def accessKey = util.execStdout('cat /etc/.saucelabs-accesskey/accesskey')
                                def url = "https://avarabyeu:$accessKey@ondemand.eu-central-1.saucelabs.com:443/wd/hub"
                                def platformName = 'Android'
                                def platformVersion = '8.1'
                                def browser = 'Chrome'
                                def deviceName = 'Samsung Galaxy S9 HD GoogleAPI Emulator'
                                def deviceOrientation = 'Portrait'

                                sh """
                                   gradle --build-cache :ui-tests:cucumber \
                                          -Dspring.profiles.active=mobile \
                                          -Dmobile.platform.name=$platformName \
                                          -Dmobile.platform.version=$platformVersion \
                                          -Dmobile.browser.name=$browser \
                                          -Dmobile.device.name="$deviceName" \
                                          -Dmobile.device.orientation=$deviceOrientation \
                                          -Dappium.server.url=$url \
                                          -Dappium.server.version=1.16.0
                                    """
                            } finally {
                                step([$class             : 'CucumberReportPublisher',
                                      jsonReportDirectory: 'ui-tests/reports',
                                      fileIncludePattern : '*.json'])
                            }
                        }
                    }
                }
            }
        }

        // Add to the service-ui ci pipeline DAST step:
        def dastJobName = 'reportportal_dast'
        stage('Run DAST') {
            catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
                println("Triggering build of SAST job: ${dastJobName}...")
                build job: dastJobName,
                        parameters: [
                                string(name: "CONFIG", value: "rp/carrier/config.yaml"),
                                string(name: "SUITE", value: "rpportal_dev_dast"),
                                booleanParam(name: "DEBUG", defaultValue: false)
                        ],
                        propagate: false, wait: false // true or false: Wait for job finish
            }
        }
    }
}
