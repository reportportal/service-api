#!groovy

node {

    load "$JENKINS_HOME/jobvars.env"

        stage('Checkout') {
            checkout scm
        }

        stage('Build') {
            docker.withServer("$DOCKER_HOST") {
                        stage('Build Docker Image') {
                            sh 'docker build -f docker/Dockerfile-develop -t reportportal-dev-5/service-api .'
                        }

                        stage('Deploy container') {
                            sh "docker-compose -p reportportal5 -f $COMPOSE_FILE_RP_5 up -d --force-recreate api"
                        }
            }

        }

}