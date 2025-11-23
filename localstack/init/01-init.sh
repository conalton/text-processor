#!/bin/bash
set -euo pipefail

REGION=${AWS_REGION}
BUCKET=${TASK_UPLOADS_LOCATION}
QUEUE=${FILE_UPLOADED_QUEUE_NAME}

echo "Creating S3 bucket ${BUCKET} in ${REGION}"
awslocal s3 mb "s3://${BUCKET}" --region "${REGION}" >/dev/null 2>&1 || true

echo "Creating / ensuring SQS queue ${QUEUE}"
awslocal sqs create-queue \
  --queue-name "${QUEUE}" \
  --region "${REGION}" >/dev/null 2>&1 || true

QUEUE_URL=$(awslocal sqs get-queue-url \
  --queue-name "${QUEUE}" \
  --region "${REGION}" \
  --query 'QueueUrl' \
  --output text)

QUEUE_ARN=$(awslocal sqs get-queue-attributes \
  --queue-url "${QUEUE_URL}" \
  --attribute-names QueueArn \
  --query 'Attributes.QueueArn' \
  --output text)

S3_SOURCE_ARN="arn:aws:s3:::${BUCKET}"
POLICY_DOC=$(cat <<EOF
{"Version":"2012-10-17","Statement":[{"Sid":"AllowS3SendMessage","Effect":"Allow","Principal":{"Service":"s3.amazonaws.com"},"Action":"sqs:SendMessage","Resource":"${QUEUE_ARN}","Condition":{"ArnEquals":{"aws:SourceArn":"${S3_SOURCE_ARN}"}}}]}
EOF
)
POLICY_ESCAPED=${POLICY_DOC//\"/\\\"}

ATTR_FILE=$(mktemp)
cat > "${ATTR_FILE}" <<EOF
{
  "VisibilityTimeout": "30",
  "Policy": "${POLICY_ESCAPED}"
}
EOF

echo "Setting attributes (VisibilityTimeout=30, Policy for bucket ${BUCKET}) for ${QUEUE}"
awslocal sqs set-queue-attributes \
  --queue-url "${QUEUE_URL}" \
  --region "${REGION}" \
  --attributes "file://${ATTR_FILE}"

rm -f "${ATTR_FILE}"

NOTIF_FILE=$(mktemp)
cat > "${NOTIF_FILE}" <<EOF
{
  "QueueConfigurations": [
    {
      "QueueArn": "${QUEUE_ARN}",
      "Events": ["s3:ObjectCreated:*"]
    }
  ]
}
EOF

echo "Configuring bucket notification: ${BUCKET} -> ${QUEUE}"
awslocal s3api put-bucket-notification-configuration \
  --bucket "${BUCKET}" \
  --notification-configuration "file://${NOTIF_FILE}"

rm -f "${NOTIF_FILE}"

echo "LocalStack infra bootstrap complete."

echo "LocalStack infra bootstrap complete."