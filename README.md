# BookStore — Distributed E-Commerce Application

A distributed e-commerce application for browsing books, managing a shopping cart, placing orders, and processing payments.

The application uses a React frontend and Spring Boot microservices, with service discovery and API Gateway routing. This repository also serves as a DevOps project for containerization, automated testing, CI/CD, and cloud deployment.

## Table of Contents

- [Project Overview](#project-overview)
- [Features](#features)
- [Technology Stack](#technology-stack)
- [Application Architecture](#application-architecture)
- [Repository Structure](#repository-structure)
- [Microservices](#microservices)
- [Local Development](#local-development)
- [Docker Deployment](#docker-deployment)
- [Environment Configuration](#environment-configuration)
- [API Gateway and Service Discovery](#api-gateway-and-service-discovery)
- [Payment Integration](#payment-integration)
- [Testing and Verification](#testing-and-verification)
- [DevOps and CI/CD Roadmap](#devops-and-cicd-roadmap)
- [Planned AWS Architecture](#planned-aws-architecture)
- [Security](#security)
- [Monitoring and Observability](#monitoring-and-observability)
- [Troubleshooting](#troubleshooting)
- [Known Limitations](#known-limitations)
- [Contributing](#contributing)
- [License](#license)

---

## Project Overview

BookStore is a distributed e-commerce application that allows users to browse books, maintain a shopping cart, provide shipping information, manage payment methods, and place orders.

The original application is built with React, Java, Spring Boot, and Spring Cloud. Its backend functionality is divided into independently deployable microservices that communicate through service discovery and API routing.

The project is also being developed as a DevOps implementation exercise, focusing on:

- Containerizing frontend and backend applications.
- Building reproducible Docker images.
- Testing application components.
- Automating build and deployment workflows.
- Publishing versioned images to Amazon Elastic Container Registry (Amazon ECR).
- Deploying backend services to Amazon Elastic Kubernetes Service (Amazon EKS).
- Hosting the frontend through Amazon S3 and Amazon CloudFront.
- Integrating a managed relational database.
- Improving application security, observability, and operational reliability.

**Deployment status:** The local Docker-based application has been brought up and the checkout flow has been exercised successfully. The AWS architecture and automated CI/CD workflow described below represent the intended deployment design and should not be considered deployed until implemented and verified.

## Features

The application includes functionality for:

- Browsing the book catalog.
- Viewing book details.
- Managing a shopping cart.
- User authentication and account management.
- Providing shipping information.
- Managing payment methods.
- Placing orders.
- Viewing order information and payment receipts.
- Processing payments through Stripe.

The available functionality depends on the state of the individual microservices and their configuration.

## Technology Stack

| Layer | Technology |
|---|---|
| Frontend | React 17 |
| Frontend development server | Create React App / React Scripts |
| Static frontend hosting locally | Nginx |
| Backend | Java, Spring Boot |
| Distributed application framework | Spring Cloud |
| API routing | Zuul-based API Gateway |
| Service discovery | Eureka or Consul, depending on configuration |
| Inter-service communication | HTTP and Spring Cloud integrations |
| Backend build | Maven |
| Frontend package management | npm |
| Containerization | Docker |
| Payment processing | Stripe |
| CI/CD target | GitHub Actions |
| Container registry target | Amazon ECR |
| Container orchestration target | Amazon EKS |
| Frontend hosting target | Amazon S3 and CloudFront |
| Database target | Amazon Aurora MySQL-compatible edition |
| Monitoring and tracing | Spring Boot Actuator, Spring Cloud Sleuth, Zipkin-compatible tracing |

The application uses older Spring Boot and Spring Cloud dependencies. Dependency compatibility should be reviewed before undertaking framework upgrades.

---

## Application Architecture

### Current local architecture

The local implementation uses Docker containers for the frontend and backend services.

```text
                         Browser
                            |
                            v
                  http://localhost:3000
                            |
                            v
                  Frontend Nginx Container
                            |
                            |
                  React Static Application
                            |
                            | HTTP API requests
                            v
                  http://localhost:8765
                            |
                            v
                     API Gateway
                            |
                 Service Discovery
                            |
             +--------------+--------------+
             |              |              |
             v              v              v
        Account Service  Catalog Service  Order Service
             |              |              |
             +--------------+--------------+
                            |
                   Additional Services
                            |
             +--------------+--------------+
             |              |              |
             v              v              v
        Billing Service Payment Service  Eureka
                            |
                            v
                       Stripe API
```

The diagram is a logical overview. Actual routes and communication paths depend on the service configuration.

The frontend Nginx container serves the compiled React application and supports client-side routing. API requests are configured separately to reach the backend API Gateway.

### Request flow

A typical request follows this sequence:

1. The user opens the React application in a browser.
2. The frontend sends an API request to the API Gateway.
3. The API Gateway routes the request to the appropriate backend service.
4. Service discovery resolves registered services where configured.
5. The selected service performs its business operation.
6. The response is returned through the gateway to the frontend.

For checkout, the order workflow also interacts with payment functionality.

### Service discovery

The original project supports Eureka and Consul in different execution configurations.

- **Eureka:** Used in the current local setup.
- **Consul:** Supported by the original Docker-oriented configuration.

The discovery mechanism must be consistent with the active service profiles and environment configuration. The presence of both technologies in the repository does not mean that both must run simultaneously.

---

## Repository Structure

The repository contains the frontend, backend microservices, shared modules, and DevOps-related files.

```text
sample-book-store-app/
│
├── bookstore-frontend-react-app/
│   ├── public/
│   ├── src/
│   ├── package.json
│   ├── package-lock.json
│   ├── Dockerfile
│   ├── nginx.conf
│   └── .dockerignore
│
├── bookstore-account-service/
├── bookstore-api-gateway-service/
├── bookstore-billing-service/
├── bookstore-catalog-service/
├── bookstore-eureka-discovery-service/
├── bookstore-order-service/
├── bookstore-payment-service/
│
├── bookstore-commons/
├── bookstore-feign/
│
├── devops/
│   ├── docker/
│   │   └── backend/
│   └── secrets/
│
├── docs/
├── docker-compose.yml
├── pom.xml
├── README.md
└── ...
```

The exact contents may change as the DevOps implementation develops.

### Important directories

| Directory | Purpose |
|---|---|
| `bookstore-frontend-react-app/` | React application and frontend container configuration |
| `bookstore-api-gateway-service/` | API Gateway and routing |
| `bookstore-eureka-discovery-service/` | Eureka discovery server |
| `bookstore-account-service/` | Account and authentication functionality |
| `bookstore-catalog-service/` | Book catalog functionality |
| `bookstore-order-service/` | Order processing |
| `bookstore-payment-service/` | Payment functionality |
| `bookstore-billing-service/` | Billing functionality |
| `bookstore-commons/` | Shared backend functionality |
| `bookstore-feign/` | Shared inter-service communication functionality |
| `devops/docker/backend/` | Backend Docker build configuration |
| `devops/secrets/` | Local secret configuration; must not be committed |
| `docs/` | Project and deployment documentation |

---

## Microservices

The application contains the following deployable backend services.

| Service | Local port | Responsibility |
|---|---:|---|
| Eureka Discovery Server | 8761 | Service registration and discovery |
| API Gateway | 8765 | API routing |
| Account Service | 4001 | User accounts and authentication |
| Billing Service | 5001 | Billing functionality |
| Catalog Service | 6001 | Book catalog |
| Order Service | 7001 | Order processing |
| Payment Service | 8001 | Payment operations |

The ports above describe the current local configuration. Container ports, service names, and Kubernetes service ports may differ in other environments.

The shared `bookstore-commons` and `bookstore-feign` modules support the application but are not standalone business services.

---

## Local Development

### Prerequisites

Install the following tools:

- Git
- Java compatible with the repository's Spring Boot dependencies
- Maven
- Node.js and npm
- Docker Desktop

For Apple Silicon Macs, use compatible ARM64 images where available. Docker can use multi-platform images when necessary, but image architecture should be verified before deployment.

### 1. Clone the repository

```bash
git clone https://github.com/soumyaparida00/sample-book-store-app.git

cd sample-book-store-app
```

### 2. Build the backend

From the repository root:

```bash
mvn clean install
```

This builds the Maven modules and packages their artifacts.

If tests fail, inspect the failure before deciding whether to skip them. Skipping tests is useful for diagnosing build compatibility, but it is not a substitute for a passing test suite.

### 3. Start the backend services

The backend services must be configured with compatible application profiles, service discovery settings, and any required environment variables.

For local execution, start the Eureka Discovery Server before the dependent services.

The expected service ports are:

- Eureka: `8761`
- API Gateway: `8765`
- Account: `4001`
- Billing: `5001`
- Catalog: `6001`
- Order: `7001`
- Payment: `8001`

Use the application's configuration files to determine the appropriate active profile for each service.

### 4. Start the frontend without Docker

Open another terminal:

```bash
cd bookstore-frontend-react-app
```

Install the dependencies using the repository's npm lockfile:

```bash
npm ci
```

Start the development server:

```bash
npm start
```

The development server normally runs at:

http://localhost:3000

Configure the backend URL using the environment-variable mechanism supported by the React application.

For example, the current frontend supports:

```env
REACT_APP_BACKEND_API_GATEWAY_URL=http://localhost:8765
```

React environment variables are embedded in the frontend build. Changes generally require restarting the development server or rebuilding the application.

---

## Docker Deployment

The project includes Docker configuration for the frontend and backend.

### Frontend container

The frontend Dockerfile uses a multi-stage build:

1. Install frontend dependencies.
2. Build the React application.
3. Copy the generated static files into an Nginx image.
4. Serve the application over HTTP.

This approach keeps Node.js and the development dependencies out of the final runtime image.

From the frontend directory:

```bash
docker build \
  --build-arg REACT_APP_BACKEND_API_GATEWAY_URL=http://localhost:8765 \
  -t bookstore-frontend:dev .
```

If Stripe integration is enabled, provide the appropriate **test-mode publishable key** through the supported build configuration. Do not provide a Stripe secret key to the frontend build.

Start the frontend container:

```bash
docker run -d \
  --name bookstore-frontend \
  --network bookstore-net \
  -p 3000:80 \
  --restart unless-stopped \
  bookstore-frontend:dev
```

The `bookstore-net` network must already exist:

```bash
docker network inspect bookstore-net
```

If it does not exist, create it:

```bash
docker network create bookstore-net
```

Do not recreate a network that already exists.

Open the frontend:

http://localhost:3000

Check the Nginx health endpoint:

```bash
curl -i --max-time 10 http://localhost:3000/health
```

Expected response:

```text
HTTP/1.1 200 OK
```

The body should contain:

```text
healthy
```

### Backend container images

The backend Docker configuration is located in:

```text
devops/docker/backend/
```

The common backend Dockerfile expects a prebuilt JAR file to be supplied through the `JAR_FILE` build argument.

For example, a module-specific build can use the following pattern:

```bash
docker build \
  -f devops/docker/backend/catalog.Dockerfile \
  -t bookstore-catalog:dev \
  bookstore-catalog-service
```

Use the correct Dockerfile, module context, and JAR path for each service.

### Start and inspect containers

List running containers:

```bash
docker ps
```

List all containers, including stopped containers:

```bash
docker ps -a
```

Inspect a service's logs:

```bash
docker logs --tail 100 bookstore-catalog
```

Follow the logs continuously:

```bash
docker logs -f bookstore-catalog
```

Inspect an individual container:

```bash
docker inspect bookstore-catalog
```

Avoid printing environment-variable values from containers because they may contain credentials.

### Verify the API Gateway

The current catalog health endpoint can be checked through the API Gateway:

```bash
curl -i --max-time 10 \
  http://localhost:8765/api/catalog/actuator/health
```

Expected response:

```json
{
  "status": "UP"
}
```

This verifies the catalog health endpoint through the gateway. It does not prove that every business API or payment operation is working.

### Docker Compose

The repository contains a legacy `docker-compose.yml`.

Review its service definitions, profiles, discovery configuration, and environment variables before using it. The existence of the file does not guarantee that it matches the newer local Docker setup.

Do not start multiple overlapping sets of containers without checking existing container names, ports, and networks first.

---

## Environment Configuration

The application requires environment-specific configuration for service discovery, API routing, and payment processing.

### Frontend configuration

The frontend currently supports:

```env
REACT_APP_BACKEND_API_GATEWAY_URL=http://localhost:8765
```

Stripe.js can be configured with:

```env
REACT_APP_STRIPE_PUBLISHABLE_KEY=pk_test_REPLACE_WITH_YOUR_TEST_PUBLISHABLE_KEY
```

The Stripe value above is a placeholder, not a working key.

Only publishable Stripe keys belong in frontend configuration. Anything embedded in a React build can be inspected by browser users.

### Backend configuration

The Payment Service requires:

```env
STRIPE_SECRET_KEY=REPLACE_WITH_YOUR_TEST_SECRET_KEY
```

Use a valid, appropriately permissioned test-mode secret key from the same Stripe account as the frontend's publishable key.

Never commit real keys or paste them into documentation.

The local payment environment may also include service discovery settings, such as the Eureka URL and instance hostname.

Keep these values in the appropriate local configuration rather than hardcoding environment-specific values into source code.

### Protect local secrets

The local payment environment file is:

```text
devops/secrets/payment.env
```

It must remain excluded from Git.

Verify the ignore rule:

```bash
git check-ignore -v devops/secrets/payment.env
```

If it is not ignored, add this entry to the root `.gitignore`:

```gitignore
/devops/secrets/payment.env
```

Then verify again.

Ignoring a file does not remove it from Git history if it was committed previously. If a real secret was exposed, rotate it.

For production deployment, use a managed secret store and grant workloads only the permissions they require.

---

## API Gateway and Service Discovery

The API Gateway provides a single entry point for frontend API requests.

For example, the catalog health endpoint is available locally through:

```text
http://localhost:8765/api/catalog/actuator/health
```

The Gateway routes requests according to its configured route definitions and service discovery settings.

### Eureka dashboard

Open:

http://localhost:8761

Use the dashboard to inspect registered services.

A service may be running as a Docker container but still be unavailable to other services if it has not registered correctly or if its advertised hostname cannot be resolved.

### Troubleshooting discovery

Check the following when a request fails:

1. The target service is running.
2. The service is attached to the expected Docker network.
3. Its active profile uses the correct discovery mechanism.
4. Its registration hostname is reachable from the other containers.
5. The Gateway route matches the requested path.
6. The service's application logs do not show startup or connectivity failures.

Do not assume that a service's host-published port is the correct address for communication between containers.

---

## Payment Integration

The application integrates with Stripe to process payment operations.

The frontend uses Stripe.js to collect payment information, while the backend performs server-side payment operations.

### Test-mode configuration

Use matching test-mode credentials from the same Stripe account:

- Frontend: `pk_test_...`
- Backend: `sk_test_...`

A restricted key such as `rk_test_...` may not have permission to perform the API operations required by the application.

Do not use live-mode credentials during development.

### Stripe test card

For a standard successful Stripe test payment, use:

| Field | Value |
|---|---|
| Card number | `4242 4242 4242 4242` |
| Expiry date | `12/30` |
| CVC | `123` |
| Postal code | `500019` |

These details are for Stripe test mode only. Never use real payment-card details for development testing.

The Stripe Dashboard can be used to inspect test-mode payment activity and receipts.

### Payment verification

After placing an order:

1. Verify that the frontend displays the order details.
2. Check the payment status shown by the application.
3. Inspect the Stripe Dashboard for the corresponding test payment.
4. Check the Payment Service logs if the request fails.
5. Confirm that the amount sent to Stripe matches the order total.

A successful Stripe receipt alone does not prove that the application's order state, inventory, and billing records have all been updated correctly.

---

## Testing and Verification

Testing should cover the frontend, backend services, service discovery, and checkout flow.

### Frontend build

From `bookstore-frontend-react-app/`:

```bash
npm ci
npm run build
```

A successful build generates the production frontend assets.

### Frontend tests

Run:

```bash
npm test
```

The project uses React Scripts and Jest-based testing.

### Backend build

From the repository root:

```bash
mvn clean install
```

Review the test results and build output. Do not treat a successful build with tests skipped as evidence that the application has passed its tests.

### Service health checks

Eureka:

```bash
curl -i --max-time 10 http://localhost:8761/
```

Catalog through the Gateway:

```bash
curl -i --max-time 10 \
  http://localhost:8765/api/catalog/actuator/health
```

Payment Service:

```bash
curl -i --max-time 10 \
  http://localhost:8001/actuator/health
```

The expected response for a correctly configured health endpoint is an HTTP success response and a body indicating that the service is up.

### End-to-end checkout

Verify the complete workflow:

1. Open the application.
2. Sign in.
3. Browse the catalog.
4. Add a book to the cart.
5. Provide shipping information.
6. Select or add a payment method.
7. Place the order using Stripe test mode.
8. Verify the order details.
9. Verify the payment in Stripe test mode.
10. Confirm that the order total and charged amount match.

---

## DevOps and CI/CD Roadmap

The DevOps implementation aims to automate the process from source-code changes to deployment.

### Intended workflow

```text
Developer Push / Pull Request
             |
             v
        GitHub Actions
             |
             v
     Checkout Source Code
             |
             v
      Build and Test
             |
             v
      Build Docker Images
             |
             v
       Publish to ECR
             |
             v
       Deploy to EKS
             |
             v
     Verify Deployment
             |
             v
       Application Access
```

The final pipeline should use separate stages or jobs for validation, building, publishing, and deployment.

### Planned GitHub Actions stages

| Stage | Responsibility |
|---|---|
| Checkout | Retrieve the source code |
| Frontend validation | Install dependencies, run tests, build the frontend |
| Backend validation | Build Maven modules and run applicable tests |
| Image build | Build service-specific Docker images |
| Image publication | Publish versioned images to Amazon ECR |
| Deployment | Update Kubernetes workloads in EKS |
| Verification | Check rollout status and application health |

The workflow should fail when a required test, build, or deployment verification step fails.

### Image tagging

Images should use immutable identifiers such as the Git commit SHA.

Example:

```text
<account-id>.dkr.ecr.<region>.amazonaws.com/bookstore-catalog:<commit-sha>
```

Avoid relying exclusively on the `latest` tag for production deployments.

### AWS authentication

GitHub Actions should authenticate to AWS using OpenID Connect (OIDC) where possible.

The IAM role should have only the permissions required by the workflow. Avoid storing long-lived AWS access keys in repository files.

The ECR publishing workflow and the EKS deployment workflow should be configured with appropriate permissions and protected environments.

---

## Planned AWS Architecture

The target architecture separates frontend hosting, backend execution, and persistent data storage.

**This is the planned architecture, not a claim that the resources have already been provisioned.**

```text
                         Users
                           |
                           v
                    Amazon CloudFront
                           |
                           v
                       Amazon S3
                    React Static Files


                         API Requests
                              |
                              v
                  Public Application Entry
                    Application Load Balancer
                              |
                              v
                    Amazon EKS Cluster
                    Private Subnets
                              |
          +-------------------+-------------------+
          |                   |                   |
          v                   v                   v
     API Gateway         Catalog Service      Account Service
          |                   |                   |
          +-------------------+-------------------+
                              |
               +--------------+--------------+
               |              |              |
               v              v              v
          Order Service  Payment Service  Billing Service
               |
               v
       Aurora MySQL-Compatible Database
       Private Database Subnets
```

The exact routing and placement of services should be finalized during infrastructure design.

### Networking

The planned network design should include:

- A dedicated Amazon VPC.
- Public subnets for the required internet-facing entry points.
- Private subnets for application workloads.
- Private database subnets.
- Internet Gateway for public connectivity.
- NAT Gateway where private workloads require outbound internet access.
- Security groups restricting traffic to required ports and sources.
- Appropriate subnet routes and network ACLs.

Availability Zones should be selected to support the desired resilience.

NAT Gateways, load balancers, EKS nodes, and managed databases can incur charges even when application traffic is low. Review the cost implications before provisioning them.

### Amazon EKS

The backend microservices are intended to run as Kubernetes workloads.

The deployment design should include:

- Kubernetes Deployments for stateless services.
- Kubernetes Services for internal communication.
- An appropriate ingress or load-balancing solution.
- ConfigMaps for non-sensitive configuration.
- Kubernetes Secrets or an integrated secret-management solution for sensitive configuration.
- Resource requests and limits.
- Readiness and liveness probes.
- Rolling deployments and rollout verification.
- Horizontal Pod Autoscaling where justified by measured demand.

Service replicas and scaling policies should be configured based on the resource requirements and behavior of each service.

### Amazon ECR

Amazon ECR will store the backend and any required frontend container images.

The pipeline should authenticate to ECR, publish versioned images, and deploy only images that have passed the required validation steps.

### Frontend: Amazon S3 and CloudFront

The React production build can be hosted as static assets in Amazon S3 and distributed through CloudFront.

The deployment process should:

1. Build the React application.
2. Upload the production build to the designated S3 bucket.
3. Apply the required bucket and CloudFront configuration.
4. Invalidate cached assets when necessary.
5. Verify that the application loads correctly.

The production API endpoint must be configured during the frontend build. A localhost API URL is appropriate only for local development.

### Database: Amazon Aurora MySQL-Compatible

Aurora MySQL-compatible is the planned managed relational database.

The database should be placed in private database subnets and should not be directly accessible from the public internet.

The design should cover:

- Database credentials and secret management.
- Network access from authorized workloads.
- Backups and retention.
- Encryption at rest and in transit.
- Database connection limits.
- Schema migrations.
- Recovery and monitoring.

The existing application's database configuration and schema must be reviewed before migrating to Aurora. A managed database is not a drop-in replacement for an embedded local development database without compatibility testing.

### Production readiness

Before exposing the application publicly, validate:

- Authentication and authorization.
- Payment security.
- Database persistence.
- HTTPS.
- CORS and allowed origins.
- Resource limits and scaling.
- Secrets management.
- Logging and monitoring.
- Backup and recovery.
- Deployment rollback.
- AWS cost controls.

---

## Security

Security controls are required throughout development and deployment.

### Credentials

Never commit:

- Stripe secret keys.
- AWS access keys.
- Database passwords.
- Production tokens.
- Private certificates.
- Environment files containing credentials.

Use placeholders in examples and store actual credentials in appropriate local or managed secret stores.

### Frontend configuration

React environment variables prefixed with `REACT_APP_` are embedded into the client-side bundle.

They must not contain confidential credentials.

Stripe publishable keys are intended for frontend use. Stripe secret keys must remain on the backend.

Any OAuth client secret embedded in browser JavaScript is also publicly inspectable and must not be treated as confidential.

### Containers

Production containers should:

- Run with the least privileges required.
- Avoid embedding secrets in image layers.
- Use appropriately maintained base images.
- Pin or control image versions.
- Be scanned for known vulnerabilities.
- Expose only necessary ports.

### AWS permissions

Use IAM roles and least-privilege policies for workloads and automation.

Do not give CI/CD workflows unrestricted AWS permissions.

### Dependency security

The frontend and backend contain dependencies that should be checked regularly for vulnerabilities.

Review security advisories and test dependency updates before merging them. Avoid automatically applying major framework upgrades without compatibility testing.

---

## Monitoring and Observability

The original application documentation describes monitoring and tracing options including:

- Spring Boot Actuator.
- Prometheus.
- Grafana.
- Zipkin.
- Telegraf.
- InfluxDB.
- Chronograf.
- Kapacitor.

These tools are optional components and should be configured explicitly for the environment in which they are used.

### Application health

Health endpoints can help verify whether individual services have started successfully.

For example:

```bash
curl -i http://localhost:8001/actuator/health
```

A successful response indicates that the health endpoint is responding. It does not necessarily prove that every downstream dependency is functioning.

### Logging

Inspect container logs when debugging:

```bash
docker logs --tail 100 bookstore-gateway
docker logs --tail 100 bookstore-order
docker logs --tail 100 bookstore-payment
```

Avoid sharing logs that contain authentication headers, access tokens, personal information, or secrets.

### Distributed tracing

Spring Cloud Sleuth can attach trace information to requests. Zipkin can collect and display traces when configured and reachable.

Warnings about a missing Zipkin endpoint may occur when tracing is enabled but Zipkin is not running. These warnings should be distinguished from errors that prevent business operations from completing.

### Production monitoring

For AWS, the monitoring design should include appropriate metrics, logs, alerts, and dashboards for:

- Application availability.
- Request failures and latency.
- Container restarts.
- CPU and memory utilization.
- Pod health.
- Database connectivity and performance.
- Payment failures.
- Deployment failures.

Select monitoring tools based on operational requirements rather than deploying every available monitoring stack by default.

---

## Troubleshooting

### Frontend does not load

Check the container:

```bash
docker ps --filter name=bookstore-frontend
```

Inspect logs:

```bash
docker logs --tail 100 bookstore-frontend
```

Test the health endpoint:

```bash
curl -i --max-time 10 http://localhost:3000/health
```

### API Gateway does not respond

```bash
docker ps --filter name=bookstore-gateway
docker logs --tail 100 bookstore-gateway
```

Verify that the Gateway is listening on port `8765` and that its discovery configuration matches the running services.

### Catalog requests fail

Test the service directly:

```bash
curl -i --max-time 10 http://localhost:6001/actuator/health
```

Then test through the Gateway:

```bash
curl -i --max-time 10 \
  http://localhost:8765/api/catalog/actuator/health
```

Compare the results to determine whether the problem lies in the service or its gateway route.

### Payment Service does not respond

```bash
docker ps --filter name=bookstore-payment
docker logs --tail 100 bookstore-payment
```

Check its health endpoint:

```bash
curl -i --max-time 10 http://localhost:8001/actuator/health
```

A connection reset or timeout can indicate that the service is still starting or has failed. Inspect startup logs before attempting another checkout.

### Stripe permission errors

A restricted Stripe key may not have permission to create a PaymentIntent.

Verify that:

- The backend uses a valid, appropriately permissioned test-mode secret key.
- The frontend uses the corresponding publishable test key.
- Both keys belong to the same Stripe account and mode.
- The secret key is not exposed to the frontend.

### Order placement returns to checkout

Inspect the request in the browser's Developer Tools:

1. Open the Network tab.
2. Select Fetch/XHR.
3. Enable Preserve log.
4. Click Place Order.
5. Identify the failed request.
6. Inspect its HTTP status and response.

Check the logs for the Order Service, Payment Service, and Gateway around the same time.

Possible causes include invalid request data, authorization failures, payment errors, service connectivity problems, and unexpected frontend response handling.

### Zipkin connection refused

If the application attempts to send traces to `localhost:9411` from inside a container, that address refers to the container itself, not automatically to another container.

Configure the tracing endpoint to use a reachable service hostname when Zipkin is running in Docker. If tracing is not required for local testing, consider disabling it through the appropriate configuration.

### Docker containers cannot communicate

Verify the network:

```bash
docker network inspect bookstore-net
```

Verify the participating containers:

```bash
docker ps
```

Container-to-container communication should use the correct service or container hostname and internal port. Host-published ports are primarily for access from the host machine.

---

## Known Limitations

The following items should be reviewed as the project progresses:

- The repository uses older Spring Boot and Spring Cloud dependencies.
- Dependency vulnerability reports require review and remediation.
- The existing application configuration should be audited before production deployment.
- Local development profiles and production profiles require different configuration.
- Payment processing must be tested against matching Stripe test-mode credentials.
- Order totals must match the amounts submitted to Stripe.
- Database persistence and migration behavior must be verified before switching to a managed database.
- The legacy Docker Compose configuration may not match the newer container setup.
- Monitoring components require explicit configuration.
- AWS deployment resources, Kubernetes manifests, and CI/CD workflows must be implemented and tested before they can be considered production-ready.

---

## Contributing

When modifying the application:

1. Create a dedicated branch.
2. Make changes in small, reviewable increments.
3. Run the applicable frontend and backend tests.
4. Verify Docker builds.
5. Check configuration and secret handling.
6. Review the Git diff before committing.
7. Document changes to service interfaces, environment variables, and deployment configuration.

Do not commit credentials, generated build artifacts, or local environment files.

---

## License

Refer to the repository's license file and original project licensing terms before redistributing or deploying the application.

If no license file exists, confirm the applicable license with the original project owner before treating the code as open source.