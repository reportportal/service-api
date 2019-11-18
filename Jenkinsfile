#!groovy

node {

    load "$JENKINS_HOME/jobvars.env"

        stage('Checkout') {
            checkout scm
        }

        stage('Build') {
            docker.withServer("$DOCKER_HOST") {
                        stage('Build Docker Image') {
                            sh 'docker build -f docker/Dockerfile-develop -t reportportal-dev-5-1/service-api .'
                        }

                        stage('Deploy container') {
                            sh "docker-compose -p reportportal5-1 -f $COMPOSE_FILE_RP_5_1 up -d --force-recreate api"
                        }
            }

        }

}
