/*
 * Copyright 2019 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
    stage('Push to ECR') {
        docker.withServer("$DOCKER_HOST") {
            withEnv(["AWS_URI=${AWS_URI}", "AWS_REGION=${AWS_REGION}"]) {
                def image = env.AWS_URI + '/service-api'
                def url = 'https://' + env.AWS_URI
                def credentials = 'ecr:' + env.AWS_REGION + ':aws_credentials'
                sh "./gradlew buildDocker -P dockerServerUrl=$DOCKER_HOST -P dockerTag=image"
                docker.withRegistry(url, credentials) {
                    docker.image(image).push('RC-${BUILD_NUMBER}')
                }
            }
        }
    }
}