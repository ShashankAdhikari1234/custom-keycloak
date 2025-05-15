# Build stage
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder

WORKDIR /build
# Copy pom.xml first to leverage Docker cache
COPY pom.xml .
# Copy source code
COPY src ./src

# Build the JAR
RUN mvn clean package

# Final stage
FROM quay.io/keycloak/keycloak:26.0.1

# Copy the built JAR from the builder stage
COPY --from=builder /build/target/custom-user-storage.jar /opt/keycloak/providers/

# Set up admin credentials using new environment variable names
ENV KC_BOOTSTRAP_ADMIN_USERNAME=admin
ENV KC_BOOTSTRAP_ADMIN_PASSWORD=admin

RUN /opt/keycloak/bin/kc.sh build

ENTRYPOINT ["/opt/keycloak/bin/kc.sh", "start-dev"]