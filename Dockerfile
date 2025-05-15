FROM quay.io/keycloak/keycloak:latest

# Copy our custom provider JAR to Keycloak's providers directory
COPY target/custom-user-storage.jar /opt/keycloak/providers/