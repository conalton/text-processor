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
- **Database Migrations**: Schema evolution managed with Flyway
- **Integration Testing**: Isolated database tests via Testcontainers
- **Code Quality**: Consistent formatting enforced with Spotless
- **CI/CD Automation**: Pipeline that checks code style and runs the full test suite

## Tech Stack

- **Java 21**
- **Spring Boot 4.0.0**
- **Spring Data JPA** with Hibernate
- **MySQL 8.0.41**
- **AWS SDK** (S3, SQS) via Spring Cloud AWS
- **LocalStack** for local AWS emulation
- **Resilience4j** for rate limiting
- **Swagger/OpenAPI 3** for API documentation
- **Gradle** for build management
- **Flyway** for database migrations
- **Testcontainers** for integration testing
- **Spotless** for code style automation

## Prerequisites

- Java 21 or higher
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

This command creates:
- S3 bucket: `text-processor-uploads`
- SQS queue: `text-processor-tasks`

### 4. Database Migrations

The application uses Flyway to automatically apply database migrations on startup, so there is no need for any manual steps to keep the schema up to date. If you want to add a new migration script, run: ```bash ./run.sh migration-add ``` This will prompt you for a readable migration name (e.g., add_users_table) and create a file named `V<timestamp>__<sanitized_name>.sql` inside `src/main/resources/db/migration`.

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
│   │   ├── java/
│   │   │   └── org/
│   │   │       └── conalton/
│   │   │           └── textprocessor/
│   │   │               ├── AppApplication.java                 # Spring Boot entry point
│   │   │               ├── common/
│   │   │               │   └── annotation/                     # e.g. Internal.java
│   │   │               ├── config/                             # Configuration / test setup (e.g. MysqlContainerConfig)
│   │   │               ├── domain/
│   │   │               │   ├── messaging/                      # Messaging interfaces, ports, message envelope
│   │   │               │   └── storage/                        # Storage properties, port, services (KeyGenerator, etc.)
│   │   │               ├── infrastructure/
│   │   │               │   ├── aws/                            # AWS integrations (S3, SQS, property configs)
│   │   │               │   └── persistence/                    # DB-specific constraints and classifiers
│   │   │               ├── modules/
│   │   │               │   └── task/                           # Task domain: controllers, services, entities, listeners
│   │   │               └── web/
│   │   │                   ├── advice/                         # Exception handlers, controller advices
│   │   │                   └── response/                       # Shared response payloads (ApiResponse, ErrorResponse)
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-dev.yml
│   │       └── db/
│   │           └── migration/                                  # Flyway SQL migration scripts
│   └── test/
│       ├── java/
│       │   └── org/
│       │       └── conalton/
│       │           └── textprocessor/
│       │               ├── config/                             # e.g. container config or test configuration beans
│       │               ├── modules/
│       │               │   └── task/                           # Tests for task controllers, services, etc.
│       │               └── ...                                # Other test packages
│       └── resources/
│           └── ...                                            # Test resources if needed
├── build.gradle.kts                                            # Gradle build configuration
├── docker-compose.yaml                                          # Infrastructure services
├── run.sh                                                       # Helper script for common tasks
└── README.md                                                    # Project documentation
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

## Roadmap 
- Consider rewriting (or partially migrating) the codebase to Kotlin to enhance developer productivity and leverage more modern language features.
- Expand file processing capabilities by integrating Large Language Model (LLM) services with Spring Cloud to enable more advanced text transformations and analysis.

## License

This project is for educational purposes.

## Contact

For questions or suggestions, please open an issue in the repository.

