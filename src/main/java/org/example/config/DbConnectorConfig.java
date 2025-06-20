package org.example.config;

/*
 * @Created At 20/06/2025
 * @Author ashim.gotame
 */

import org.example.constants.Constants;
import org.example.provider.CustomUserStorageProvider;
import org.jboss.logging.Logger;
import org.keycloak.component.ComponentModel;

import java.sql.Connection;
import java.sql.DriverManager;

public class DbConnectorConfig {
    private final ComponentModel model;

    private static final Logger logger = Logger.getLogger(DbConnectorConfig.class);

    public DbConnectorConfig(ComponentModel model) {
        this.model = model;
    }

    public Connection getConnection() throws Exception {
        String url = model.getConfig().getFirst(Constants.DATABASE_URL);
        String user = model.getConfig().getFirst(Constants.DATABASE_USER);
        String password = model.getConfig().getFirst(Constants.DATABASE_PASSWORD);

        logger.infof("Connecting to DB URL: %s with user: %s", url, user);
        return DriverManager.getConnection(url, user, password);
    }
}
