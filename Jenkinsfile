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
        sh "./gradlew buildDocker"
    }
    stage('Push to registries') {
        withEnv(["AWS_URI=${AWS_URI}", "AWS_REGION=${AWS_REGION}"]) {
            sh 'docker tag reportportal-dev/service-api ${AWS_URI}/service-api:SNAPSHOT-${BUILD_NUMBER}'
            def image = env.AWS_URI + '/service-api' + ':SNAPSHOT-' + env.BUILD_NUMBER
            def url = 'https://' + env.AWS_URI
            def credentials = 'ecr:' + env.AWS_REGION + ':aws_credentials'
            echo image
            docker.withRegistry(url, credentials) {
                docker.image(image).push()
            }
        }
    }
    stage('Cleanup') {
        withEnv(["AWS_URI=${AWS_URI}"]) {
            sh 'docker rmi ${AWS_URI}/service-api:SNAPSHOT-${BUILD_NUMBER}'
            sh 'docker rmi reportportal-dev/service-api:latest'
        }
    }
}
