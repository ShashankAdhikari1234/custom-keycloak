package org.example.utils;

/*
 * @Created At 20/06/2025
 * @Author ashim.gotame
 */

import org.example.config.DbConnectorConfig;
import org.jboss.logging.Logger;
import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.storage.adapter.AbstractUserAdapterFederatedStorage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KeyCloakUtils {

    private static final Logger logger = Logger.getLogger(KeyCloakUtils.class);

    private final KeycloakSession session;

    private final DbConnectorConfig dbConnectorConfig;

    private final ComponentModel model;

    public KeyCloakUtils(KeycloakSession session, DbConnectorConfig dbConnectorConfig, ComponentModel model) {
        this.session = session;
        this.dbConnectorConfig = dbConnectorConfig;
        this.model = model;
    }

    public UserModel fetchUserFromDatabase(RealmModel realm, String field, String value) {
        logger.infof("Fetching user from DB by %s = %s", field, value);
        try (Connection connection = dbConnectorConfig.getConnection();
             PreparedStatement stmt = connection.prepareStatement("SELECT * FROM users WHERE " + field + " = ?")) {

            stmt.setString(1, value);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    logger.info("User found in DB, mapping to UserModel");
                    return mapToUserModel(realm, rs);
                } else {
                    logger.info("No user found in DB for " + field + "=" + value);
                }
            }
        } catch (Exception e) {
            logger.error("Error fetching user from database", e);
        }
        return null;
    }

    public UserModel mapToUserModel(RealmModel realm, ResultSet rs) {
        logger.info("Mapping ResultSet row to UserModel");

        return new AbstractUserAdapterFederatedStorage(session, realm, model) {

            private String username;
            private String email;
            private String firstName;
            private String lastName;
            private String branch;

            {
                try {
                    username = rs.getString("username");
                    email = rs.getString("email");
                    firstName = rs.getString("first_name");
                    lastName = rs.getString("last_name");
                    branch = rs.getString("branch");
                } catch (SQLException e) {
                    throw new RuntimeException("Error initializing user fields from ResultSet", e);
                }
            }

            @Override
            public String getUsername() {
                logger.debug("[Adapter] Getting username");
                return username;
            }

            @Override
            public void setUsername(String username) {
                this.username = username;
            }

            @Override
            public String getEmail() {
                return email;
            }

            @Override
            public void setEmail(String email) {
                this.email = email;
            }

            @Override
            public String getFirstName() {
                return firstName;
            }

            @Override
            public void setFirstName(String firstName) {
                this.firstName = firstName;
            }

            @Override
            public String getLastName() {
                return lastName;
            }

            @Override
            public void setLastName(String lastName) {
                this.lastName = lastName;
            }

            @Override
            public Map<String, List<String>> getAttributes() {
                logger.debug("[Adapter] Getting all attributes");

                Map<String, List<String>> attributes = new HashMap<>(super.getAttributes());

                if (branch != null && !branch.isEmpty()) {
                    attributes.put("branch", Collections.singletonList(branch));
                }

                return attributes;
            }

            @Override
            public String getFirstAttribute(String name) {
                if ("branch".equalsIgnoreCase(name) && branch != null) {
                    return branch;
                }
                return super.getFirstAttribute(name);
            }

            @Override
            public void setAttribute(String name, List<String> values) {
                logger.debugf("[Adapter] Setting attribute %s = %s", name, values);
                getFederatedStorage().setAttribute(realm, getId(), name, values);
            }

        };
    }



}
