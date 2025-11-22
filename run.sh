#!/usr/bin/env bash
set -euo pipefail

if [[ $# -ne 1 ]]; then
  echo "Usage: $0 {debug|test|migration-add}"
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
    SPRING_PROFILES_ACTIVE=test ./gradlew test --rerun
    ;;
  migration-add)
    read -rp "Enter migration name (e.g., add_users_table): " MIGRATION_NAME
    if [[ -z "${MIGRATION_NAME// }" ]]; then
      echo "Migration name cannot be empty."
      exit 1
    fi

    SANITIZED_NAME=$(echo "$MIGRATION_NAME" \
      | tr '[:upper:]' '[:lower:]' \
      | tr -cs 'a-z0-9_' '_' \
      | sed -e 's/_\+/_/g' -e 's/^_//' -e 's/_$//')

    TIMESTAMP=$(date +"%Y%m%d%H%M%S")
    TARGET_DIR="src/main/resources/db/migration"
    FILE_PATH="$TARGET_DIR/V${TIMESTAMP}__${SANITIZED_NAME}.sql"

    mkdir -p "$TARGET_DIR"

    if [[ -e "$FILE_PATH" ]]; then
      echo "File already exists: $FILE_PATH"
      exit 1
    fi

    {
      echo "-- Migration: $MIGRATION_NAME"
      echo "-- Created at: $(date -Iseconds)"
      echo
    } > "$FILE_PATH"

    echo "Migration file created: $FILE_PATH"
    ;;
  *)
    echo "Unknown mode: $1"
    echo "Allowed values: debug, test, migration-add"
    exit 1
    ;;
esac