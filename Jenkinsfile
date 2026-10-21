pipeline {
    agent any

    options {
        timestamps()
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '10'))
    }

    triggers {
        githubPush()
        pollSCM('H/2 * * * *')
    }

    environment {
        DOCKERHUB_USER       = 'rakhith'
        DOCKER_IMAGE         = "${DOCKERHUB_USER}/calculator"
        IMAGE_TAG            = "${BUILD_NUMBER}"
        COMPOSE_PROJECT_NAME = 'calculator'
        JAVA_HOME            = '/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home'
        DOCKER_HOST          = 'unix:///Users/rakshith/.docker/run/docker.sock'
        PATH                 = "/opt/homebrew/bin:/usr/local/bin:/Applications/Docker.app/Contents/Resources/bin:${PATH}"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build') {
            steps {
                sh 'mvn -B clean package -DskipTests'
            }
        }

        stage('Test') {
            steps {
                sh 'mvn -B test'
            }
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                }
            }
        }

        stage('Docker Build') {
            steps {
                sh 'docker build -t $DOCKER_IMAGE:$IMAGE_TAG -t $DOCKER_IMAGE:latest .'
            }
        }

        stage('Push') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'dockerhub-creds',
                                                  usernameVariable: 'DH_USER',
                                                  passwordVariable: 'DH_PASS')]) {
                    sh '''
                        # temporary docker config so login does not touch the macOS keychain
                        export DOCKER_CONFIG="$WORKSPACE/.docker-ci"
                        mkdir -p "$DOCKER_CONFIG"
                        echo "$DH_PASS" | docker login -u "$DH_USER" --password-stdin
                        docker push $DOCKER_IMAGE:$IMAGE_TAG
                        docker push $DOCKER_IMAGE:latest
                        docker logout
                        rm -rf "$DOCKER_CONFIG"
                    '''
                }
            }
        }

        stage('Deploy') {
            steps {
                sh '''
                    docker compose up -d --remove-orphans
                    for i in $(seq 1 12); do
                        if curl -fs http://localhost:8082/health | grep -q UP; then
                            echo "Deployment is healthy"
                            exit 0
                        fi
                        echo "Waiting for health check ($i/12)..."
                        sleep 5
                    done
                    echo "Health check failed"
                    docker compose ps
                    exit 1
                '''
            }
        }
    }

    post {
        success {
            mail to: 'rakshithsrinivasan2004@gmail.com',
                 subject: "SUCCESS: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                 body: "Build #${env.BUILD_NUMBER} succeeded and was deployed.\nImage: ${env.DOCKER_IMAGE}:${env.IMAGE_TAG}\nDetails: ${env.BUILD_URL}"
        }
        failure {
            mail to: 'rakshithsrinivasan2004@gmail.com',
                 subject: "FAILED: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                 body: "Build #${env.BUILD_NUMBER} failed.\nCheck the console output: ${env.BUILD_URL}console"
        }
    }
}
