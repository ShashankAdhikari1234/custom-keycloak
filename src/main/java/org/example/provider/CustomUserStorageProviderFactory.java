package org.example.provider;

import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;
import org.keycloak.storage.UserStorageProviderFactory;

import java.util.List;

public class CustomUserStorageProviderFactory
        implements UserStorageProviderFactory<CustomUserStorageProvider> {

    public static final String PROVIDER_ID = "custom-db-provider";

    static final String DATABASE_URL = "databaseUrl";
    static final String DATABASE_USER = "databaseUser";
    static final String DATABASE_PASSWORD = "databasePassword";
    static final String DATABASE_SCHEMA = "databaseSchema";

    static final String DEFAULT_DATABASE_URL = "jdbc:postgresql://host.docker.internal:5432/keycloak";
    static final String DEFAULT_DATABASE_USER = "postgres";
    static final String DEFAULT_DATABASE_PASSWORD = "password";
    static final String DEFAULT_DATABASE_SCHEMA = "public";

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return ProviderConfigurationBuilder.create()
                .property(DATABASE_URL, "Database URL", "JDBC URL for the database", ProviderConfigProperty.STRING_TYPE, DEFAULT_DATABASE_URL, null)
                .property(DATABASE_USER, "Database Username", "Username for the database", ProviderConfigProperty.STRING_TYPE, DEFAULT_DATABASE_USER, null)
                .property(DATABASE_PASSWORD, "Database Password", "Password for the database", ProviderConfigProperty.PASSWORD, DEFAULT_DATABASE_PASSWORD, null)
                .property(DATABASE_SCHEMA, "Database Schema", "Schema name in the database", ProviderConfigProperty.STRING_TYPE, DEFAULT_DATABASE_SCHEMA, null)
                .build();
    }


    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public CustomUserStorageProvider create(KeycloakSession session, ComponentModel model) {
        return new CustomUserStorageProvider(session,model);
    }

    @Override
    public void validateConfiguration(KeycloakSession session, org.keycloak.models.RealmModel realm, ComponentModel model)
            throws org.keycloak.component.ComponentValidationException {
        if (model.getConfig().getFirst(DATABASE_URL) == null
                || model.getConfig().getFirst(DATABASE_USER) == null
                || model.getConfig().getFirst(DATABASE_PASSWORD) == null
                || model.getConfig().getFirst(DATABASE_SCHEMA) == null) {
            throw new org.keycloak.component.ComponentValidationException("Database URL, username, password, and schema are required");
        }
    }

    @Override
    public void onCreate(KeycloakSession session, org.keycloak.models.RealmModel realm,
                         ComponentModel model) {
        // Optional: any initialization logic
    }

    @Override
    public void close() {
        // Cleanup, if needed
    }
}
