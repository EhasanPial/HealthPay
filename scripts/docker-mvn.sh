#!/usr/bin/env bash
#
# Runs Maven inside a Java 21 container, so Java does not need to be installed.
#
# Integration tests still work: the container gets access to Docker
# (the Docker socket), so Testcontainers can start PostgreSQL, Redis and Kafka.
#
# Usage:
#   ./scripts/docker-mvn.sh                  same as: clean verify
#   ./scripts/docker-mvn.sh test             fast tests only
#   ./scripts/docker-mvn.sh clean verify     all tests
#   ./scripts/docker-mvn.sh clean package    build the jar
#
# Optional environment variables:
#   DOCKER_SOCK                      path to the Docker socket (default /var/run/docker.sock)
#   TESTCONTAINERS_RYUK_DISABLED     set to true only if Testcontainers' cleanup container cannot start

set -euo pipefail

cd "$(dirname "$0")/.."

if [ "$#" -eq 0 ]; then
  set -- clean verify
fi

DOCKER_SOCK="${DOCKER_SOCK:-/var/run/docker.sock}"
HOST_USER="$(id -u):$(id -g)"

docker run --rm \
  --volume "$PWD":/workspace \
  --workdir /workspace \
  --volume healthpay-maven-cache:/root/.m2 \
  --volume "$DOCKER_SOCK":/var/run/docker.sock \
  --add-host host.docker.internal:host-gateway \
  --env TESTCONTAINERS_HOST_OVERRIDE=host.docker.internal \
  --env TESTCONTAINERS_RYUK_DISABLED="${TESTCONTAINERS_RYUK_DISABLED:-false}" \
  --env HOST_USER="$HOST_USER" \
  eclipse-temurin:21-jdk \
  bash -c '
    chmod +x mvnw
    ./mvnw -B "$@"
    status=$?
    # Files created inside the container belong to root. Give target/ back to you.
    [ -d target ] && chown -R "$HOST_USER" target
    exit $status
  ' bash "$@"
