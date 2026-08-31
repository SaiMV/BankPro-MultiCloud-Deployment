pipeline {
    agent any

    parameters {
        string(name: 'DOCKERHUB_USERNAME',
               defaultValue: 'saimullassery',
               description: 'DockerHub username')
        string(name: 'IMAGE_TAG',
               defaultValue: '3.0',
               description: 'Docker image tag')
        string(name: 'AWS_HOST',
               defaultValue: 'AWS_PUBLIC_IP',
               description: 'AWS VM public IP or DNS')
        string(name: 'AZURE_HOST',
               defaultValue: 'AZURE_PUBLIC_IP',
               description: 'Azure VM public IP or DNS')
    }

    environment {
        IMAGE_NAME = "${params.DOCKERHUB_USERNAME}/bankpro-banking-app"
        IMAGE = "${IMAGE_NAME}:${params.IMAGE_TAG}"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build & Test') {
            steps {
                sh 'mvn -B clean test package'
            }
        }

        stage('Docker Build') {
            steps {
                sh 'docker build -t ${IMAGE} .'
            }
        }

        stage('Docker Login & Push') {
            steps {
                withCredentials([usernamePassword(
                    credentialsId: 'dockerhub-credentials',
                    usernameVariable: 'DOCKER_USER',
                    passwordVariable: 'DOCKER_PASSWORD'
                )]) {
                    sh '''
                        echo "$DOCKER_PASSWORD" | docker login -u "$DOCKER_USER" --password-stdin
                        docker push "$IMAGE"
                        docker logout
                    '''
                }
            }
        }

        stage('Prepare Ansible') {
            steps {
                sh '''
                    ansible-galaxy collection install -r ansible/requirements.yml
                    cat > ansible/jenkins-inventory.ini <<EOF
[aws]
aws_vm ansible_host=${AWS_HOST}

[azure]
azure_vm ansible_host=${AZURE_HOST}

[multicloud:children]
aws
azure

[multicloud:vars]
ansible_python_interpreter=/usr/bin/python3
EOF
                '''
            }
        }

        stage('Deploy to AWS & Azure') {
            steps {
                withCredentials([sshUserPrivateKey(
                    credentialsId: 'multicloud-ssh-key',
                    keyFileVariable: 'SSH_KEY',
                    usernameVariable: 'SSH_USER'
                )]) {
                    sh '''
                        test "$AWS_HOST" != "AWS_PUBLIC_IP"
                        test "$AZURE_HOST" != "AZURE_PUBLIC_IP"

                        ANSIBLE_HOST_KEY_CHECKING=False ansible-playbook \
                          -i ansible/jenkins-inventory.ini \
                          ansible/deploy.yml \
                          -u "$SSH_USER" \
                          --private-key "$SSH_KEY" \
                          -e "docker_image=$IMAGE"
                    '''
                }
            }
        }
    }

    post {
        success {
            echo 'BankPro multi-cloud deployment completed successfully.'
        }
        failure {
            echo 'Pipeline failed. Review the Jenkins console output.'
        }
    }
}
