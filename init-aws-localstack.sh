#!/bin/bash

docker-compose exec -it localstack awslocal s3 mb s3://text-cleaner-uploads --region eu-central-1
docker-compose exec -it localstack awslocal sqs create-queue --queue-name text-cleaner-tasks --region eu-central-1
