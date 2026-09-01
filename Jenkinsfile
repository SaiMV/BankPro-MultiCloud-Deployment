pipeline {
    agent any

    parameters {
    string(name: 'DOCKERHUB_USERNAME',
           defaultValue: 'saimullassery',
           description: 'DockerHub username')

    string(name: 'IMAGE_TAG',
           defaultValue: '3.3',
           description: 'Docker image tag')

    string(name: 'AWS_PUBLIC_IP',
           defaultValue: '100.54.120.122',
           description: 'AWS VM public IP or DNS')

    string(name: 'AZURE_HOST',
           defaultValue: 'AZURE_PUBLIC_IP',
           description: 'Azure VM public IP or DNS')
}

    environment {
        DOCKER_IMAGE = 'saimullassery/bankpro-banking-app'
    IMAGE_TAG = "${params.IMAGE_TAG}"
    AWS_PUBLIC_IP = "${params.AWS_PUBLIC_IP}"
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
        sh '''
            docker build \
              -t ${DOCKER_IMAGE}:${IMAGE_TAG} .
        '''
    }
}

       stage('Docker Build & Push') {
    steps {
        withCredentials([usernamePassword(
            credentialsId: 'dockerhub-credentials',
            usernameVariable: 'DOCKER_USERNAME',
            passwordVariable: 'DOCKER_PASSWORD'
        )]) {
            sh '''
                set -e

                echo "$DOCKER_PASSWORD" | docker login \
                    -u "$DOCKER_USERNAME" \
                    --password-stdin

                docker buildx build \
                    --platform linux/amd64 \
                    -t ${DOCKERHUB_USERNAME}/bankpro-banking-app:${IMAGE_TAG} \
                    --push \
                    .

                docker buildx imagetools inspect \
                    ${DOCKERHUB_USERNAME}/bankpro-banking-app:${IMAGE_TAG}
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

        // stage('Deploy to AWS & Azure') {
        //     steps {
        //         withCredentials([sshUserPrivateKey(
        //             credentialsId: 'multicloud-ssh-key',
        //             keyFileVariable: 'SSH_KEY',
        //             usernameVariable: 'SSH_USER'
        //         )]) {
        //             sh '''
        //                 test "$AWS_HOST" != "AWS_PUBLIC_IP"
        //                 test "$AZURE_HOST" != "AZURE_PUBLIC_IP"

        //                 ANSIBLE_HOST_KEY_CHECKING=False ansible-playbook \
        //                   -i ansible/jenkins-inventory.ini \
        //                   ansible/deploy.yml \
        //                   -u "$SSH_USER" \
        //                   --private-key "$SSH_KEY" \
        //                   -e "docker_image=$IMAGE"
        //             '''
        //         }
        //     }
        // }
//         stage('Deploy to AWS') {
//     steps {
//         withCredentials([
//             sshUserPrivateKey(
//                 credentialsId: 'multicloud-ssh-key',
//                 keyFileVariable: 'SSH_KEY',
//                 usernameVariable: 'SSH_USER'
//             )
//         ]) {
//             sh '''
//                 set -e

//                 echo "Deploying BankPro to AWS EC2..."

//                 test -n "${AWS_PUBLIC_IP}"

//                 chmod 600 "$SSH_KEY"

//                 ssh -o StrictHostKeyChecking=no \
//                     -i "$SSH_KEY" \
//                     "${SSH_USER}@${AWS_PUBLIC_IP}" \
//                     "docker pull ${DOCKER_IMAGE}:${IMAGE_TAG} && \
//                      docker stop bankpro-banking-app || true && \
//                      docker rm bankpro-banking-app || true && \
//                      docker run -d \
//                        --name bankpro-banking-app \
//                        --restart unless-stopped \
//                        -p 8080:8080 \
//                        ${DOCKER_IMAGE}:${IMAGE_TAG}"

//                 echo "Waiting for application..."
//                 sleep 15

//                 curl -fsS "http://${AWS_PUBLIC_IP}:8080/api/health"

//                 echo ""
//                 echo "AWS deployment successful!"
//             '''
//         }
//     }
// }
//     }
stage('Deploy to AWS') {
    steps {
        withCredentials([
            sshUserPrivateKey(
                credentialsId: 'multicloud-ssh-key',
                keyFileVariable: 'SSH_KEY',
                usernameVariable: 'SSH_USER'
            )
        ]) {
            sh '''
                set -e

                echo "========================================="
                echo "Deploying BankPro to AWS EC2"
                echo "========================================="

                test -n "${AWS_PUBLIC_IP}"

                echo "AWS Host: ${AWS_PUBLIC_IP}"
                echo "Docker Image: ${DOCKER_IMAGE}:${IMAGE_TAG}"

                chmod 600 "$SSH_KEY"

                ssh -o StrictHostKeyChecking=no \
    -i "$SSH_KEY" \
    "${SSH_USER}@${AWS_PUBLIC_IP}" \
    "set -e
     echo 'Pulling Docker image...'
     docker pull ${DOCKER_IMAGE}:${IMAGE_TAG}

     echo 'Removing existing container...'
     docker rm -f bankpro-banking-app 2>/dev/null || true

     echo 'Starting BankPro...'
     docker run -d \
       --name bankpro-banking-app \
       --restart unless-stopped \
       -p 8080:8080 \
       ${DOCKER_IMAGE}:${IMAGE_TAG}

     docker ps
    "

                echo "Waiting for BankPro application..."
                sleep 20

                echo "Running health check..."
                curl -fsS \
                    "http://${AWS_PUBLIC_IP}:8080/api/health"

                echo ""
                echo "========================================="
                echo "AWS DEPLOYMENT SUCCESSFUL"
                echo "========================================="
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
