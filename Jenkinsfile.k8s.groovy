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
//              containerTemplate(name: 'kubectl', image: 'lachlanevenson/k8s-kubectl:v1.8.8', command: 'cat', ttyEnabled: true),
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

        properties([
                pipelineTriggers([
                        pollSCM('H/10 * * * *')
                ])
        ])

        stage('Configure') {
            container('docker') {
                sh 'echo "Initialize environment"'
                sh """
                QUAY_USER=\$(cat "/etc/.dockercreds/username")
                cat "/etc/.dockercreds/password" | docker login -u \$QUAY_USER --password-stdin quay.io
                """
            }
        }
        parallel 'Checkout Infra': {
            stage('Checkout Infra') {
                sh 'mkdir -p ~/.ssh'
                sh 'ssh-keyscan -t rsa github.com >> ~/.ssh/known_hosts'
                dir('kubernetes') {
                    git branch: "v5", url: 'https://github.com/reportportal/kubernetes.git'

                }
            }
        }, 'Checkout Service': {
            stage('Checkout Service') {
                dir('app') {
                    checkout scm
                }
            }
        }


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
                        version = """${
                            sh(
                                    returnStdout: true,
                                    script: 'cat gradle.properties | grep "version" | cut -d "=" -f2'
                            )
                        }""".trim()
                        image = "quay.io/reportportal/service-api:${version}-BUILD-${env.BUILD_NUMBER}"
                        sh "docker build -f docker/Dockerfile-develop -t $image ."
                        sh "docker push $image"
                    }

                }
            }


        }
    }
}

