#!groovy

node {

    load "$JENKINS_HOME/jobvars.env"

    env.JAVA_HOME = "${tool 'openjdk-11'}"
    env.PATH = "${env.JAVA_HOME}/bin:${env.PATH}"

    stage('Checkout') {
        checkout scm
    }
    stage('Assemble') {
        sh "./gradlew clean assemble -P buildNumber=${env.BUILD_NUMBER}"
    }
    stage('Test') {
        sh './gradlew test --full-stacktrace'
    }
    stage('Build') {
        sh './gradlew build'
    }
    stage('Docker image') {
        sh "./gradlew buildDocker -P dockerServerUrl=$DOCKER_HOST"
    }
    stage('Deploy Container') {
        docker.withServer("$DOCKER_HOST") {
            sh "docker-compose -p reportportal -f $COMPOSE_FILE_RP up -d --force-recreate api"

            stage('Push to registries') {
                withEnv(["AWS_URI=${AWS_URI}", "AWS_REGION=${AWS_REGION}", "LOCAL_REGISTRY=${LOCAL_REGISTRY}"]) {
                    sh 'docker tag reportportal-dev/service-api ${AWS_URI}/service-api'
                    sh 'docker tag reportportal-dev/service-api ${LOCAL_REGISTRY}/service-api'
                    sh 'docker push ${LOCAL_REGISTRY}/service-api'
                    def image = env.AWS_URI + '/service-api'
                    def url = 'https://' + env.AWS_URI
                    def credentials = 'ecr:' + env.AWS_REGION + ':aws_credentials'
                    docker.withRegistry(url, credentials) {
                        docker.image(image).push('SNAPSHOT-${BUILD_NUMBER}')
                    }
                }
            }
        }
    }
    stage('Cleanup') {
        docker.withServer("$DOCKER_HOST") {
            withEnv(["AWS_URI=${AWS_URI}", "LOCAL_REGISTRY=${LOCAL_REGISTRY}"]) {
                sh 'docker rmi ${AWS_URI}/service-api:SNAPSHOT-${BUILD_NUMBER}'
                sh 'docker rmi ${AWS_URI}/service-api:latest'
                sh 'docker rmi ${LOCAL_REGISTRY}/service-api:latest'
            }
        }
    }
}