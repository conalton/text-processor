#!/bin/bash

# Load .env file and export variables only for the child process
set -a  # automatically export all variables
source .env
set +a  # stop auto-exporting

# Run bootRun with the loaded environment
./gradlew bootRun
