#!/usr/bin/env groovy
def call(String name = 'app') {
    def gitlabTokenCredsID = "gitlab-token-id"
    def devAWSAccessKeyID = "dev-aws_access_key_id"
    def devAWSSecretAccessKey = "dev-aws_secret_access_key"

    pipeline {
        agent {
            label 'docker-slave'
        }

        parameters {
            string(name: 'FUNCTION_NAME', defaultValue: 'deploy', description: 'This parameter specific function name only to deploy')

            booleanParam(name: 'MIGRATE', defaultValue: false, description: 'This parameter to deploy migration or not')
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
                    withCredentials([usernamePassword(credentialsId: gitlabTokenCredsID,
                                                usernameVariable: 'USERNAME',
                                                passwordVariable: 'PASSWORD')]) {                    
                        sh 'echo "machine gitlab.com login $USERNAME password $PASSWORD" >> ~/.netrc'
                    }
                    withCredentials([string(credentialsId: devAWSAccessKeyID, variable: 'AWS_ACCESS_KEY_ID')]) {
                        sh 'aws configure set aws_access_key_id $AWS_ACCESS_KEY_ID --profile $AWS_PROFILE'
                    }
                    withCredentials([string(credentialsId: devAWSSecretAccessKey, variable: 'AWS_SECRET_ACCESS_KEY')]) {
                        sh 'aws configure set aws_secret_access_key $AWS_SECRET_ACCESS_KEY --profile $AWS_PROFILE'
                    }
                    sh 'aws configure set region $AWS_REGION --profile $AWS_PROFILE'
                    sh 'aws configure set output json --profile $AWS_PROFILE'
                }
            }
            stage('Get Depends') {
                steps {
                    sh 'make depends'
                    sh 'rm -rf ./scripts'
                    sh 'git clone https://gitlab.com/bnpl-hdbank/api/cicd-docs.git ./cicd'
                    sh 'cp -r ./cicd/scripts ./scripts'
                }
            }
            stage('Migration') {
                when {
                    // This stage is processed only when Migrate is true
                    environment name: 'MIGRATE', value: 'true'
                }
                steps {
                    sh 'make dev.migrate'
                }
            }
            stage('Deploy') {
                steps {
                    sh "make dev.${params.FUNCTION_NAME}"
                }
            }
        }
    }
}