#!/bin/bash

docker-compose exec -it localstack awslocal s3 mb s3://text-processor-uploads --region eu-central-1
docker-compose exec -it localstack awslocal sqs create-queue --queue-name text-processor-tasks --region eu-central-1
