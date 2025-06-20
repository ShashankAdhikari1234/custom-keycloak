package org.example.provider;

import org.example.config.DbConnectorConfig;
import org.example.utils.KeyCloakUtils;
import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;
import org.keycloak.storage.UserStorageProviderFactory;

import java.util.List;

import static org.example.constants.Constants.*;

public class CustomUserStorageProviderFactory
        implements UserStorageProviderFactory<CustomUserStorageProvider> {

    public static final String PROVIDER_ID = "custom-db-provider";


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
    public CustomUserStorageProvider create(KeycloakSession session, ComponentModel model) {
        DbConnectorConfig dbConfig = new DbConnectorConfig(model);
        KeyCloakUtils cloakUtils = new KeyCloakUtils(session, dbConfig,model);
        return new CustomUserStorageProvider(dbConfig, cloakUtils);
    }
    @Override
    public String getId() {
        return PROVIDER_ID;
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
