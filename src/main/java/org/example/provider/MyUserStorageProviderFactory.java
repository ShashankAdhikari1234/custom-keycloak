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
import org.keycloak.utils.StringUtil;

import java.util.List;

/**
 * @author sashank.adhikari on 5/6/2025
 */
@AutoService(UserStorageProviderFactory.class)
public class MyUserStorageProviderFactory implements UserStorageProviderFactory<MyUserStorageProvider> {
    private static final Logger logger = Logger.getLogger(MyUserStorageProviderFactory.class);

    static final String PROVIDER_ID = "my-user-storage-provider";
    static final String DATABASE_URL = "databaseUrl";
    static final String DATABASE_USER = "databaseUser";
    static final String DATABASE_PASSWORD = "databasePassword";

    static final String DEFAULT_DATABASE_URL = "jdbc:postgresql://localhost:5432/";
    static final String DEFAULT_DATABASE_USER = "postgres";
    static final String DEFAULT_DATABASE_PASSWORD = "root";

    @Override
    public String getId() {
        logger.info("Initializing custom user storage provider");
        return PROVIDER_ID;
    }

    @Override
    public MyUserStorageProvider create(KeycloakSession session, ComponentModel model) {
        return new MyUserStorageProvider(session, model);
    }

    @Override
    public void init(Config.Scope config) {
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
    }

    @Override
    public void close() {
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return ProviderConfigurationBuilder.create()
                .property(DATABASE_URL, "db-url", "dbUrlHelp", ProviderConfigProperty.STRING_TYPE, DEFAULT_DATABASE_URL, null)
                .property(DATABASE_USER, "db-username", "dbUsernameHelp", ProviderConfigProperty.STRING_TYPE, DEFAULT_DATABASE_USER, null)
                .property(DATABASE_PASSWORD, "db-password", "dbPasswordHelp", ProviderConfigProperty.STRING_TYPE, DEFAULT_DATABASE_PASSWORD, null)
                .build();
    }

    @Override
    public void validateConfiguration(KeycloakSession session, RealmModel model, ComponentModel modelConfig) {
        if (StringUtil.isBlank(modelConfig.get(DATABASE_URL)) || StringUtil.isBlank(modelConfig.get(DATABASE_USER)) || StringUtil.isBlank(modelConfig.get(DATABASE_PASSWORD))) {
            throw new ComponentValidationException("Configuration invalid or lacks required fields");
        }
    }

}
