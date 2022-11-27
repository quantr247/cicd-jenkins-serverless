pipeline {
    agent {
        label 'docker-slave'
    }

    environment {
        PATH = "${env.PATH}:/usr/local/go/bin"
        GO111MODULE = "on"
        GOPRIVATE = "gitlab.com"
    }

    stages {
        stage('Pre Run') {
            steps {
                sh 'touch ~/.netrc'
                sh 'chmod 600 ~/.netrc'
                sh 'echo "machine gitlab.com login ${env.GITLAB_USER} password ${env.GITLAB_TOKEN}" >> ~/.netrc'
                sh 'aws configure set aws_access_key_id ${env.AWS_ACCESS_KEY_ID} --profile ${env.AWS_PROFILE}'
                sh 'aws configure set aws_secret_access_key ${env.AWS_SECRET_ACCESS_KEY} --profile ${env.AWS_PROFILE}'
                sh 'aws configure set region ${env.AWS_REGION} --profile ${env.AWS_PROFILE}'
                sh 'aws configure set output json --profile ${env.AWS_PROFILE}'
            }
        }
        stage('Get Depends') {
            steps {
                sh 'make depends'
            }
        }
        stage('Migration') {
            steps {
                sh 'make dev.migrate'
            }
        }
        stage('Deploy') {
            steps {
                sh 'make dev.deploy'
            }
        }
    }
}