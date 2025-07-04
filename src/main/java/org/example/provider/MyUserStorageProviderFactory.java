package org.example.provider;

import com.google.auto.service.AutoService;
import org.keycloak.Config;
import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;
import org.keycloak.storage.UserStorageProviderFactory;
import org.jboss.logging.Logger;

import java.util.List;

/**
 * @author sashank.adhikari on 5/6/2025
 */
@AutoService(UserStorageProviderFactory.class)
public class MyUserStorageProviderFactory implements UserStorageProviderFactory <MyUserStorageProvider> {
    private static final Logger logger = Logger.getLogger(MyUserStorageProviderFactory.class);

    @Override
    public String getId() {
        logger.info("Initializing custom user storage provider");
        return "user-provider";
    }

    @Override
    public MyUserStorageProvider create(KeycloakSession session, ComponentModel model) {
        return new MyUserStorageProvider(session, model);
    }

    @Override
    public void init(Config.Scope config) {}

    @Override
    public void postInit(KeycloakSessionFactory factory) {}

    @Override
    public void close() {}

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return ProviderConfigurationBuilder.create().build();
    }

}
