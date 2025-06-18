package org.example.provider;

import com.google.auto.service.AutoService;
import org.jboss.logging.Logger;
import org.keycloak.Config;
import org.keycloak.component.ComponentModel;
import org.keycloak.component.ComponentValidationException;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.RealmModel;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;
import org.keycloak.storage.UserStorageProviderFactory;
import java.util.List;

@AutoService(UserStorageProviderFactory.class)
public class CustomDBFederationProviderFactory implements UserStorageProviderFactory<CustomDbProvider> {

    private static final Logger logger = Logger.getLogger(CustomDBFederationProviderFactory.class);

    public static final String PROVIDER_ID = "custom-db-federation";
    public static final String DATABASE_URL = "databaseUrl";
    public static final String DATABASE_USER = "databaseUser";
    public static final String DATABASE_PASSWORD = "databasePassword";

    private static final String DEFAULT_DATABASE_URL = "jdbc:postgresql://localhost:5432/keycloak";
    private static final String DEFAULT_DATABASE_USER = "postgres";
    private static final String DEFAULT_DATABASE_PASSWORD = "password";

    @Override
    public String getId() {
        logger.info("Initializing custom user db storage provider");
        return PROVIDER_ID;
    }

    @Override
    public CustomDbProvider create(KeycloakSession session, ComponentModel model) {
        return new CustomDbProvider(session, model);
    }

    @Override
    public void init(Config.Scope config) {}

    @Override
    public void postInit(KeycloakSessionFactory factory) {}

    @Override
    public void close() {}

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return ProviderConfigurationBuilder.create()
                .property(DATABASE_URL, "Database URL", "JDBC connection string", ProviderConfigProperty.STRING_TYPE, DEFAULT_DATABASE_URL, null)
                .property(DATABASE_USER, "Database User", "Username for DB", ProviderConfigProperty.STRING_TYPE, DEFAULT_DATABASE_USER, null)
                .property(DATABASE_PASSWORD, "Database Password", "Password for DB", ProviderConfigProperty.STRING_TYPE, DEFAULT_DATABASE_PASSWORD, null)
                .build();
    }

    @Override
    public void validateConfiguration(KeycloakSession session, RealmModel realm, ComponentModel config) {
        List<String> requiredProps = List.of(DATABASE_URL, DATABASE_USER, DATABASE_PASSWORD);
        for (String key : requiredProps) {
            if (config.get(key) == null || config.get(key).isBlank()) {
                throw new ComponentValidationException("Missing required config property: " + key);
            }
        }
    }
}
