#!groovy

//String podTemplateConcat = "${serviceName}-${buildNumber}-${uuid}"
def label = "worker-${UUID.randomUUID().toString()}"
println("label")
println("${label}")

podTemplate(
        label: "${label}",
        containers: [
                containerTemplate(name: 'jnlp', image: 'jenkins/jnlp-slave:alpine'),
                containerTemplate(name: 'docker', image: 'docker:dind', ttyEnabled: true, alwaysPullImage: true, privileged: true,
                        command: 'dockerd --host=unix:///var/run/docker.sock --host=tcp://0.0.0.0:2375 --storage-driver=overlay',
                        resourceRequestCpu: '500m',
                        resourceLimitCpu: '800m',
                        resourceRequestMemory: '2048Mi',
                        resourceLimitMemory: '2048Mi'),
//                containerTemplate(name: 'jdk', image: 'quay.io/reportportal/openjdk-8-alpine-nonroot', command: 'cat', ttyEnabled: true),
//                containerTemplate(name: 'gradle', image: 'quay.io/reportportal/gradle-nonroot', command: 'cat', ttyEnabled: true),
              containerTemplate(name: 'kubectl', image: 'lachlanevenson/k8s-kubectl:v1.8.8', command: 'cat', ttyEnabled: true),
              containerTemplate(name: 'helm', image: 'lachlanevenson/k8s-helm:latest', command: 'cat', ttyEnabled: true)
        ],
        imagePullSecrets: ["regcred"],
        volumes: [
                emptyDirVolume(memory: false, mountPath: '/var/lib/docker'),
                secretVolume(mountPath: '/etc/.dockercreds', secretName: 'docker-creds'),
//                hostPathVolume(mountPath: '/home/gradle/.gradle', hostPath: '/tmp/jenkins/gradle')
        ]
) {

    node("${label}") {
        def srvRepo = "quay.io/reportportal/service-index"
        def srvVersion = "BUILD-${env.BUILD_NUMBER}"
        def tag = "$srvRepo:$srvVersion"

        def k8sDir = "kubernetes"
        def ciDir = "reportportal-ci"
        def appDir = "app"

        parallel 'Checkout Infra': {
            stage('Checkout Infra') {
                sh 'mkdir -p ~/.ssh'
                sh 'ssh-keyscan -t rsa github.com >> ~/.ssh/known_hosts'
                sh 'ssh-keyscan -t rsa git.epam.com >> ~/.ssh/known_hosts'
                dir('kubernetes') {
                    git branch: "master", url: 'https://github.com/reportportal/kubernetes.git'

                }
                dir('reportportal-ci') {
                    git credentialsId: 'epm-gitlab-key', branch: "master", url: 'git@git.epam.com:epmc-tst/reportportal-ci.git'
                }

            }
        }, 'Checkout Service': {
            stage('Checkout Service') {
                dir('app') {
                    checkout scm
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



        stage('Build Docker Image') {
            dir('app') {
                container('docker') {
                    container('docker') {
                        def snapshotVersion = utils.readProperty("gradle.properties", "version")
                        image = "quay.io/reportportal/service-api:${snapshotVersion}-BUILD-${env.BUILD_NUMBER}"
                        sh "docker build -f docker/Dockerfile-develop -t $image ."
                        sh "docker push $image"
                    }

                }
            }


        }
        stage('Deploy to Dev Environment') {
            container('helm') {
                dir('kubernetes/reportportal/v5') {
                    sh 'helm dependency update'
                }
                sh "helm upgrade --reuse-values --set serviceapi.repository=$srvRepo --set serviceapi.tag=$srvVersion --wait -f ./reportportal-ci/rp/values-ci.yml reportportal ./kubernetes/reportportal/v5"
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
                def snapshotVersion = utils.readProperty("app/gradle.properties", "version")
                test.checkVersion("http://$srvUrl", "$snapshotVersion-$srvVersion")
            }
        }

    }
}

