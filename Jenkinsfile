#!groovy

node {

    load "$JENKINS_HOME/jobvars.env"

    stage('Checkout') {
        checkout scm
    }
    stage('Test') {
        sh './gradlew clean test --full-stacktrace'
    }
    stage('Build') {
        sh "./gradlew build -P buildNumber=${env.BUILD_NUMBER}"
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
