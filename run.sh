#!/usr/bin/env bash
set -euo pipefail

if [[ $# -ne 1 ]]; then
  echo "Usage: $0 {debug|test|infra}"
  exit 1
fi

if [[ -f .env ]]; then
  set -a
  source .env
  set +a
fi

case "$1" in
  debug)
    echo "Launching Spring Boot in debug mode..."
    ./gradlew bootRun
    ;;
  test)
    echo "Running tests with the 'test' profile..."
    SPRING_PROFILES_ACTIVE=test ./gradlew test
    ;;
  infra)
    echo "Creating S3 bucket and SQS queue in LocalStack..."
    docker-compose exec -T localstack awslocal s3 mb s3://text-processor-uploads --region eu-central-1
    docker-compose exec -T localstack awslocal sqs create-queue --queue-name text-processor-tasks --region eu-central-1
    echo "Infrastructure bootstrap complete."
    ;;
  *)
    echo "Unknown mode: $1"
    echo "Allowed values: debug, test, infra"
    exit 1
    ;;
esac