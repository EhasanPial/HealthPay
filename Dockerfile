# syntax=docker/dockerfile:1

# ---------------------------------------------------------------------
# Stage 1: build the jar with Java 21 and the Maven Wrapper.
# Tests are not run here. Run them with: ./scripts/docker-mvn.sh clean verify
# ---------------------------------------------------------------------
FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw

COPY src/ src/
# The cache mount keeps downloaded Maven libraries between builds,
# without storing them inside the image.
RUN --mount=type=cache,target=/root/.m2 ./mvnw -B -DskipTests package

# ---------------------------------------------------------------------
# Stage 2: small runtime image. Only the JRE and the jar, no source code or build tools.
# ---------------------------------------------------------------------
FROM eclipse-temurin:21-jre
WORKDIR /app

RUN groupadd --system healthpay \
    && useradd --system --gid healthpay --no-create-home healthpay

COPY --from=build /workspace/target/*.jar app.jar

# Never run the application as root.
USER healthpay

EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "/app/app.jar"]
