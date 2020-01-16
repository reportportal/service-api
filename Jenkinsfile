#!groovy

node {

    load "$JENKINS_HOME/jobvars.env"

    def buildNumber = "BUILD-${env.BUILD_NUMBER}".toString()

    stage('Checkout') {
        checkout scm
    }
    stage('Test') {
        sh './gradlew clean test --full-stacktrace'
    }
    stage('Build') {
        sh "./gradlew build -P buildNumber=$buildNumber"
    }
    stage('Docker image') {
        sh "./gradlew buildDocker -P dockerServerUrl=$DOCKER_HOST"
    }
    stage('Deploy container') {
        docker.withServer("$DOCKER_HOST") {
            sh "docker-compose -p reportportal5 -f $COMPOSE_FILE_RP_5 up -d --force-recreate api"
        }
    }
}
