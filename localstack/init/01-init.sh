#!/bin/bash
set -euo pipefail

REGION=${AWS_REGION}
BUCKET=${TASK_UPLOADS_LOCATION}
QUEUE=${TASK_QUEUE_NAME}
DLQ=${TASK_DLQ_NAME}
MAX_RECEIVE=${TASK_QUEUE_MAX_RECEIVE}

echo "Creating S3 bucket ${BUCKET} in ${REGION}"
awslocal s3 mb "s3://${BUCKET}" --region "${REGION}" >/dev/null 2>&1 || true

echo "Creating / ensuring SQS DLQ ${DLQ}"
awslocal sqs create-queue \
  --queue-name "${DLQ}" \
  --region "${REGION}" >/dev/null 2>&1 || true

DLQ_URL=$(awslocal sqs get-queue-url \
  --queue-name "${DLQ}" \
  --region "${REGION}" \
  --query 'QueueUrl' \
  --output text)

DLQ_ARN=$(awslocal sqs get-queue-attributes \
  --queue-url "${DLQ_URL}" \
  --attribute-names QueueArn \
  --query 'Attributes.QueueArn' \
  --output text)

echo "Creating / ensuring SQS queue ${QUEUE}"
awslocal sqs create-queue \
  --queue-name "${QUEUE}" \
  --region "${REGION}" >/dev/null 2>&1 || true

QUEUE_URL=$(awslocal sqs get-queue-url \
  --queue-name "${QUEUE}" \
  --region "${REGION}" \
  --query 'QueueUrl' \
  --output text)

ATTR_FILE=$(mktemp)
cat > "${ATTR_FILE}" <<EOF
{
  "VisibilityTimeout": "30",
  "RedrivePolicy": "{\"deadLetterTargetArn\":\"${DLQ_ARN}\",\"maxReceiveCount\":\"${MAX_RECEIVE}\"}"
}
EOF

echo "Setting attributes (VisibilityTimeout=30, RedrivePolicy -> ${DLQ}) for ${QUEUE}"
awslocal sqs set-queue-attributes \
  --queue-url "${QUEUE_URL}" \
  --region "${REGION}" \
  --attributes "file://${ATTR_FILE}"

rm -f "${ATTR_FILE}"

echo "LocalStack infra bootstrap complete."