package org.example.provider;

import org.jboss.logging.Logger;
import org.keycloak.component.ComponentModel;
import org.keycloak.credential.CredentialInput;
import org.keycloak.credential.CredentialInputValidator;
import org.keycloak.models.GroupModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.credential.PasswordCredentialModel;
import org.keycloak.storage.UserStorageProvider;
import org.keycloak.storage.adapter.AbstractUserAdapterFederatedStorage;
import org.keycloak.storage.user.UserLookupProvider;
import org.keycloak.storage.user.UserQueryProvider;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class MyUserStorageProvider implements UserStorageProvider, UserLookupProvider, CredentialInputValidator, UserQueryProvider {

    private static final Logger logger = Logger.getLogger(MyUserStorageProvider.class);

    private final KeycloakSession session;
    private final ComponentModel model;

    public MyUserStorageProvider(KeycloakSession session, ComponentModel model) {
        this.session = session;
        this.model = model;
    }

    @Override
    public void close() {
        logger.info("MyUserStorageProvider closing");
    }

    @Override
    public UserModel getUserByUsername(RealmModel realm, String username) {
        logger.debugf("getUserByUsername called - Realm: %s, Username: %s", realm.getName(), username);
        return fetchUserFromDatabase(realm, "username", username);
    }

    @Override
    public UserModel getUserById(RealmModel realm, String id) {
        logger.debugf("getUserById called - Realm: %s, ID: %s", realm.getName(), id);
        return fetchUserFromDatabase(realm, "id", id);
    }

    @Override
    public UserModel getUserByEmail(RealmModel realm, String email) {
        logger.debugf("getUserByEmail called - Realm: %s, Email: %s", realm.getName(), email);
        return fetchUserFromDatabase(realm, "email", email);
    }

    private UserModel fetchUserFromDatabase(RealmModel realm, String field, String value) {
        logger.debugf("Fetching user from DB by %s = %s", field, value);
        try (Connection connection = getConnection();
             PreparedStatement stmt = connection.prepareStatement("SELECT * FROM users WHERE " + field + " = ?")) {

            stmt.setString(1, value);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    logger.debug("User found in DB, mapping to UserModel");
                    return mapToUserModel(realm, rs);
                } else {
                    logger.debug("No user found in DB for " + field + "=" + value);
                }
            }
        } catch (Exception e) {
            logger.error("Error fetching user from database", e);
        }
        return null;
    }

    private UserModel mapToUserModel(RealmModel realm, ResultSet rs) throws Exception {
        logger.debug("Mapping ResultSet row to UserModel");
        AbstractUserAdapterFederatedStorage user = new AbstractUserAdapterFederatedStorage(session, realm, model) {
            @Override
            public String getUsername() {
                try {
                    String username = rs.getString("username");
                    logger.debugf("Mapped username: %s", username);
                    return username;
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void setUsername(String username) {
                // no-op
            }

            @Override
            public String getId() {
                try {
                    String providerId = model.getId().toLowerCase();
                    String userId = rs.getString("id").toLowerCase();
                    String id = providerId + "::" + userId;
                    logger.debugf("Mapped user ID: %s", id);
                    return id;
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }

        };

        String email = rs.getString("email");
        String firstName = rs.getString("first_name");
        String lastName = rs.getString("last_name");

        logger.debugf("Setting user email: %s, firstName: %s, lastName: %s", email, firstName, lastName);

        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEnabled(true);

        return user;
    }

    private Connection getConnection() throws Exception {
        String url = model.getConfig().getFirst(MyUserStorageProviderFactory.DATABASE_URL);
        String user = model.getConfig().getFirst(MyUserStorageProviderFactory.DATABASE_USER);
        String password = model.getConfig().getFirst(MyUserStorageProviderFactory.DATABASE_PASSWORD);

        logger.debugf("Connecting to DB URL: %s with user: %s", url, user);
        return DriverManager.getConnection(url, user, password);
    }

    @Override
    public boolean supportsCredentialType(String credentialType) {
        return PasswordCredentialModel.TYPE.equals(credentialType);
    }

    @Override
    public boolean isConfiguredFor(RealmModel realm, UserModel user, String credentialType) {
        return supportsCredentialType(credentialType);
    }

    @Override
    public boolean isValid(RealmModel realm, UserModel user, CredentialInput input) {
        logger.debugf("Validating credentials for user %s", user.getUsername());
        if (!supportsCredentialType(input.getType())) {
            logger.debug("Unsupported credential type: " + input.getType());
            return false;
        }

        try (Connection connection = getConnection();
             PreparedStatement stmt = connection.prepareStatement("SELECT password FROM users WHERE username = ?")) {

            stmt.setString(1, user.getUsername());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String storedPassword = rs.getString("password");
                    logger.debug("Comparing passwords");
                    return input.getChallengeResponse().equals(storedPassword); // Simple compare for testing
                } else {
                    logger.debug("No password found for user");
                }
            }
        } catch (Exception e) {
            logger.error("Error validating credentials", e);
        }
        return false;
    }

    @Override
    public int getUsersCount(RealmModel realm) {
        logger.debug("Getting users count");
        try (Connection connection = getConnection();
             PreparedStatement stmt = connection.prepareStatement("SELECT COUNT(*) FROM users")) {

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt(1);
                    logger.debugf("Users count: %d", count);
                    return count;
                }
            }
        } catch (Exception e) {
            logger.error("Error fetching user count", e);
        }
        return 0;
    }

    @Override
    public Stream<UserModel> searchForUserStream(RealmModel realm, Map<String, String> params, Integer firstResult, Integer maxResults) {
        String search = params.getOrDefault(UserModel.SEARCH, "");
        logger.debugf("searchForUserStream called with search=%s, firstResult=%d, maxResults=%d", search, firstResult, maxResults);

        try (Connection connection = getConnection();
             PreparedStatement stmt = connection.prepareStatement(
                     "SELECT * FROM users WHERE username LIKE ? OR email LIKE ? LIMIT ? OFFSET ?")) {

            stmt.setString(1, "%" + search + "%");
            stmt.setString(2, "%" + search + "%");
            stmt.setInt(3, maxResults == null ? 10 : maxResults);
            stmt.setInt(4, firstResult == null ? 0 : firstResult);

            ResultSet rs = stmt.executeQuery();
            logger.debug("Query executed, mapping ResultSet to Stream<UserModel>");
            return mapToUserModelStream(realm, rs);

        } catch (Exception e) {
            logger.error("Error searching for users", e);
            return Stream.empty();
        }
    }

    private Stream<UserModel> mapToUserModelStream(RealmModel realm, ResultSet rs) {
        List<UserModel> list = new ArrayList<>();
        try {
            while (rs.next()) {
                list.add(mapToUserModel(realm, rs));
            }
        } catch (Exception e) {
            logger.error("Error mapping users", e);
            throw new RuntimeException(e);
        }
        return list.stream();
    }


    @Override
    public Stream<UserModel> getGroupMembersStream(RealmModel realm, GroupModel group, Integer firstResult, Integer maxResults) {
        logger.debug("getGroupMembersStream called but not implemented");
        return Stream.empty();
    }

    @Override
    public Stream<UserModel> searchForUserByUserAttributeStream(RealmModel realm, String attrName, String attrValue) {
        logger.debug("searchForUserByUserAttributeStream called but not implemented");
        return Stream.empty();
    }
}
