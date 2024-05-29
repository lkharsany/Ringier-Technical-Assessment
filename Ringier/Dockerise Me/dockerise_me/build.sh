#!/bin/sh

# Enable Docker BuildKit
export DOCKER_BUILDKIT=1

# Build the Docker image
docker build -t nodejs-dockerised-app:latest --progress=plain .

# Run the Dockerfile
docker run -p 8080:8080 nodejs-dockerised-app:latest

