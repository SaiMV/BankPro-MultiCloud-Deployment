# BankPro Multi-Cloud Deployment

## Course-End Project 1

**Application Deployment to Multi-Cloud**

This project demonstrates an end-to-end DevOps workflow for deploying a Spring Boot banking REST API to AWS and Azure using:

- GitHub
- Jenkins
- Maven
- Docker
- DockerHub
- Ansible
- AWS EC2
- Azure Virtual Machine
- Swagger/OpenAPI

The supplied capstone requirements call for GitHub source control, Jenkins CI/CD, Docker image build/publish, Ansible connectivity to AWS and Azure VMs, deployment to both clouds, and Swagger-based validation. 

## Architecture

```text
Developer
   |
   v
GitHub Repository
   |
   v
Jenkins Pipeline
   |
   +----> Maven Build & Test
   |
   +----> Docker Build
   |
   +----> DockerHub Push
   |
   v
Ansible
   |
   +--------------------+
   |                    |
   v                    v
AWS EC2 VM          Azure VM
   |                    |
   v                    v
Docker Container    Docker Container
   |                    |
   +---------+----------+
             |
             v
     BankPro Banking API
          Port 8080
             |
             v
      Swagger UI / API
```

## Application endpoints

Swagger UI:

```text
http://<SERVER-IP>:8080/swagger-ui.html
```

Health check:

```text
GET /api/health
```

Customers:

```text
GET  /api/customers
GET  /api/customers/{id}
POST /api/customers
```

Accounts and transactions:

```text
GET  /api/accounts
GET  /api/accounts/{id}
POST /api/accounts
GET  /api/accounts/{id}/transactions
POST /api/accounts/{id}/transactions
```

## Local build

Requirements:

- Java 17
- Maven 3.9+
- Docker

Run:

```bash
mvn clean test package
java -jar target/banking-app-1.0.0.jar
```

Open:

```text
http://localhost:8080/swagger-ui.html
```

## Docker

Build:

```bash
mvn clean package
docker build -t YOUR_DOCKERHUB_USERNAME/bankpro-banking-app:3.0 .
```

Run:

```bash
docker run -d --name bankpro-banking-app -p 8080:8080 \
  YOUR_DOCKERHUB_USERNAME/bankpro-banking-app:3.0
```

## Ansible setup

Install the required collection:

```bash
ansible-galaxy collection install -r ansible/requirements.yml
```

Edit:

```text
ansible/inventory.ini
```

Replace:

```text
AWS_PUBLIC_IP
AZURE_PUBLIC_IP
```

Also update the Docker image in `ansible/deploy.yml` or pass it through Jenkins.

Test connectivity:

```bash
ansible -i ansible/inventory.ini multicloud -m ping
```

Deploy:

```bash
ansible-playbook \
  -i ansible/inventory.ini \
  ansible/deploy.yml \
  -u <SSH_USER> \
  --private-key <SSH_KEY> \
  -e "docker_image=YOUR_DOCKERHUB_USERNAME/bankpro-banking-app:3.0"
```

## Jenkins credentials

Create these Jenkins credentials:

### DockerHub

Credential ID:

```text
dockerhub-credentials
```

Type:

```text
Username with password
```

### AWS/Azure SSH key

Credential ID:

```text
multicloud-ssh-key
```

Type:

```text
SSH Username with private key
```

Do not commit private keys, passwords, tokens, or cloud credentials to GitHub.

## Security groups / network rules

For the cloud VMs, allow:

```text
SSH      TCP 22
HTTP/API TCP 8080
```

Restrict SSH to your trusted source IP where possible.

## Expected validation

After deployment, validate both targets separately:

```text
AWS:
http://<AWS_PUBLIC_IP>:8080/swagger-ui.html

Azure:
http://<AZURE_PUBLIC_IP>:8080/swagger-ui.html
```

Also:

```text
http://<AWS_PUBLIC_IP>:8080/api/health
http://<AZURE_PUBLIC_IP>:8080/api/health
```

## Evidence to capture

1. GitHub repository
2. Jenkins successful pipeline
3. DockerHub image and tag
4. Ansible ping/deployment
5. AWS VM
6. Azure VM
7. AWS Swagger
8. Azure Swagger
9. Docker container running on AWS
10. Docker container running on Azure
