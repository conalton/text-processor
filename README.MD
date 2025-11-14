# Text Processor API

A Spring Boot application for processing text files with AWS S3 storage integration and SQS queue messaging.

## Overview

This is a pet project that demonstrates a modern Spring Boot microservice with the following features:

- **File Upload Management**: Generate presigned URLs for secure file uploads to S3
- **Task Processing**: Asynchronous task handling via SQS queues
- **Rate Limiting**: Built-in API rate limiting using Resilience4j
- **Database Integration**: MySQL for persistent storage with Hibernate/JPA
- **API Documentation**: Interactive Swagger UI for API exploration
- **Local Development**: LocalStack for AWS services emulation
- **Containerized Infrastructure**: Docker Compose for easy setup

## Tech Stack

- **Java 17**
- **Spring Boot 3.5.7**
- **Spring Data JPA** with Hibernate
- **MySQL 8.0.41**
- **AWS SDK** (S3, SQS) via Spring Cloud AWS
- **LocalStack** for local AWS emulation
- **Resilience4j** for rate limiting
- **Swagger/OpenAPI 3** for API documentation
- **Gradle** for build management

## Prerequisites

- Java 17 or higher
- Docker and Docker Compose
- Bash shell (for running scripts on Linux/WSL)

## Getting Started

### 1. Clone the Repository

```bash
git clone <repository-url>
cd txt-cleaner
```

### 2. Configure Environment Variables

Copy the sample environment file:

```bash
cp .env.sample .env
```

The `.env` file contains all necessary configuration for local development:

```env
# MySQL
MYSQL_DATABASE=appdb
MYSQL_USER=user
MYSQL_PASSWORD=password
MYSQL_ROOT_PASSWORD=rootpassword
MYSQL_PORT=3306
MYSQL_HOST=localhost

# LocalStack
LOCALSTACK_SERVICES=s3,sqs,sns,dynamodb
LOCALSTACK_DEBUG=1
LOCALSTACK_PORT=4566

# Application
SPRING_PROFILES_ACTIVE=dev

# AWS Configuration (for LocalStack)
AWS_REGION=eu-central-1
AWS_SECRET_KEY=test
AWS_ACCESS_KEY=test
AWS_HOST=localhost
```

### 3. Start Infrastructure Services

Start MySQL and LocalStack using Docker Compose:

```bash
docker-compose up -d
```

Wait a few seconds for services to be ready, then initialize the AWS infrastructure:

```bash
./run.sh infra
```

This command creates:
- S3 bucket: `text-processor-uploads`
- SQS queue: `text-processor-tasks`

### 4. Run Database Migrations

Ensure your database schema is up to date. The application uses Hibernate with `validate` mode, so you may need to create tables manually or use a migration tool like Flyway.

### 5. Run the Application

#### Development Mode (with Debug)

```bash
./run.sh debug
```

The application will start with debug agent on port 5005 and the API on port 8080.

#### Standard Mode

```bash
./gradlew bootRun
```

### 6. Run Tests

```bash
./run.sh test
```

Or using Gradle directly:

```bash
./gradlew test
```

## API Documentation

Once the application is running, access the interactive API documentation:

**Swagger UI**: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

**OpenAPI JSON**: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

## API Endpoints

### Create Task

Creates a new task and generates a presigned URL for file upload.

**Endpoint**: `POST /api/v1/text-processor/tasks/create`

**Response** (200 OK):
```json
{
  "taskId": "550e8400-e29b-41d4-a716-446655440000",
  "uploadUrl": "https://s3.amazonaws.com/text-processor-uploads/..."
}
```

**Rate Limiting**: 5 requests per 60 seconds per client

**Error Responses**:
- `429 Too Many Requests` - Rate limit exceeded
- `500 Internal Server Error` - Server error

## Project Structure

```
txt-cleaner/
├── src/
│   ├── main/
│   │   ├── java/org/conalton/textprocessor/
│   │   │   ├── controller/       # REST controllers
│   │   │   ├── service/          # Business logic
│   │   │   ├── domain/           # Domain services & factories
│   │   │   ├── repository/       # Data access layer
│   │   │   ├── entity/           # JPA entities
│   │   │   ├── dto/              # Data transfer objects
│   │   │   ├── config/           # Spring configuration
│   │   │   └── AppApplication.java
│   │   └── resources/
│   │       ├── application.yml
│   │       └── application-dev.yml
│   └── test/                     # Unit and integration tests
├── build.gradle.kts              # Gradle build configuration
├── docker-compose.yaml           # Infrastructure services
├── run.sh                        # Helper script for common tasks
└── README.MD                     # This file
```

## Development

### Code Formatting

The project uses Google Java Format. To format code:

```bash
./gradlew spotlessApply
```

To check formatting:

```bash
./gradlew spotlessCheck
```

### Building

Build the application:

```bash
./gradlew build
```

Create distribution packages:

```bash
./gradlew bootJar
```

The executable JAR will be available at `build/libs/app-1.0-SNAPSHOT.jar`

## Configuration Profiles

The application supports multiple profiles:

- **dev** (default): Uses MySQL and LocalStack for development
- **test**: Uses H2 in-memory database for testing
- **prod**: Production configuration (customize as needed)

Switch profiles by setting the `SPRING_PROFILES_ACTIVE` environment variable.

## Docker Services

### MySQL

- **Host**: localhost
- **Port**: 3306
- **Database**: appdb
- **User**: user
- **Password**: password

### LocalStack

- **Host**: localhost
- **Port**: 4566
- **Services**: S3, SQS, SNS, DynamoDB
- **Region**: eu-central-1

Access LocalStack services:

```bash
docker-compose exec localstack awslocal s3 ls
docker-compose exec localstack awslocal sqs list-queues
```

## Troubleshooting

### Services Won't Start

Check Docker logs:

```bash
docker-compose logs mysql
docker-compose logs localstack
```

### Database Connection Issues

Ensure MySQL is ready before starting the application:

```bash
docker-compose ps
```

### LocalStack Infrastructure Not Created

Re-run the infrastructure setup:

```bash
./run.sh infra
```

## License

This project is for educational purposes.

## Contact

For questions or suggestions, please open an issue in the repository.

