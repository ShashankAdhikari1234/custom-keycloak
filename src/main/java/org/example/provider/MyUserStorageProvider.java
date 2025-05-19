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
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static io.smallrye.config._private.ConfigLogging.log;

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
        logger.infof("getUserByUsername called - Realm: %s, Username: %s", realm.getName(), username);
        return fetchUserFromDatabase(realm, "username", username);
    }

    @Override
    public UserModel getUserById(RealmModel realm, String id) {
        logger.infof("getUserById called - Realm: %s, ID: %s", realm.getName(), id);
        return fetchUserFromDatabase(realm, "id", id);
    }

    @Override
    public UserModel getUserByEmail(RealmModel realm, String email) {
        logger.infof("getUserByEmail called - Realm: %s, Email: %s", realm.getName(), email);
        return fetchUserFromDatabase(realm, "email", email);
    }

    private UserModel fetchUserFromDatabase(RealmModel realm, String field, String value) {
        logger.infof("Fetching user from DB by %s = %s", field, value);
        try (Connection connection = getConnection();
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

//    private UserModel mapToUserModel(RealmModel realm, ResultSet rs) throws Exception {
//        logger.info("Mapping ResultSet row to UserModel");
//        return new AbstractUserAdapterFederatedStorage(session, realm, model) { // anonymous class inheriting the abstract parent
//            @Override
//            public String getUsername() {
//                log.infov("[Keycloak UserModel Adapter] Getting username ....");
//                System.out.println(realm.getName());
//                try {
//                    return rs.getString("username");
//                } catch (SQLException e) {
//                    throw new RuntimeException(e);
//                }
//            }
//
//            @Override
//            public String getEmail() {
//                log.infov("[Keycloak UserModel Adapter] Getting email ....");
//                System.out.println(realm.getName());
//                try {
//                    return rs.getString("email");
//                } catch (SQLException e) {
//                    throw new RuntimeException(e);
//                }
//            }
//
//            @Override
//            public String getFirstName() {
//                log.infov("[Keycloak UserModel Adapter] Getting username ....");
//                System.out.println(realm.getName());
//                try {
//                    return rs.getString("first_name");
//                } catch (SQLException e) {
//                    throw new RuntimeException(e);
//                }
//            }
//
//            @Override
//            public void setUsername(String username) {
//                log.infov("[Keycloak UserModel Adapter] Setting username: {0}", username);
//                user.setUsername(username);
//            }
//
//            @Override
//            public void setEmail(String email) {
//                log.infov("[Keycloak UserModel Adapter] Setting email: email={0}", email);
//                user.setEmail(email);
//            }
//
//            @Override
//            public Map<String, List<String>> getAttributes() {
//                log.infov("[Keycloak UserModel Adapter] Getting all attributes ....");
//                return getFederatedStorage().getAttributes(realm, this.getId());
//            }
//
//            @Override
//            public void setAttribute(String name, List<String> values) {
//                log.infov("[Keycloak UserModel Adapter] Setting attribute {0} with values {1}", name, values);
//
//                getFederatedStorage().setAttribute(realm, this.getId(), "id", Arrays.asList(user.getId().toString()));
//            }
//        };
//

    /// /        String email = rs.getString("email");
    /// /        String firstName = rs.getString("first_name");
    /// /        String lastName = rs.getString("last_name");
    /// /
    /// /        logger.infof("Setting user email: %s, firstName: %s, lastName: %s", email, firstName, lastName);
    /// /
    /// /        user.setEmail(email);
    /// /        user.setFirstName(firstName);
    /// /        user.setLastName(lastName);
    /// /        user.setEnabled(true);
//
//        return user;
//    }
    private UserModel mapToUserModel(RealmModel realm, ResultSet rs) throws Exception {
        logger.info("Mapping ResultSet row to UserModel");

        return new AbstractUserAdapterFederatedStorage(session, realm, model) {

            private String username;
            private String email;
            private String firstName;
            private String lastName;

            {
                // Initialize fields from ResultSet during object creation
                try {
                    username = rs.getString("username");
                    email = rs.getString("email");
                    firstName = rs.getString("first_name");
                    lastName = rs.getString("last_name");
                } catch (SQLException e) {
                    throw new RuntimeException("Error initializing user fields from ResultSet", e);
                }
            }

            @Override
            public String getUsername() {
                log.infov("[Keycloak UserModel Adapter] Getting username ....");
                System.out.println(realm.getName());
                return username;
            }

            @Override
            public String getEmail() {
                log.infov("[Keycloak UserModel Adapter] Getting email ....");
                System.out.println(realm.getName());
                return email;
            }

            @Override
            public String getFirstName() {
                log.infov("[Keycloak UserModel Adapter] Getting first name ....");
                System.out.println(realm.getName());
                return firstName;
            }

            @Override
            public String getLastName() {
                return lastName;
            }

            @Override
            public void setUsername(String username) {
                log.infov("[Keycloak UserModel Adapter] Setting username: {0}", username);
                this.username = username;
            }

            @Override
            public void setEmail(String email) {
                log.infov("[Keycloak UserModel Adapter] Setting email: email={0}", email);
                this.email = email;
            }

            @Override
            public void setLastName(String lastName) {
                this.lastName = lastName;
            }

            @Override
            public Map<String, List<String>> getAttributes() {
                log.infov("[Keycloak UserModel Adapter] Getting all attributes ....");
                return getFederatedStorage().getAttributes(realm, this.getId());
            }

            @Override
            public void setAttribute(String name, List<String> values) {
                log.infov("[Keycloak UserModel Adapter] Setting attribute {0} with values {1}", name, values);
                getFederatedStorage().setAttribute(realm, this.getId(), name, values);
            }
        };
    }


    private Connection getConnection() throws Exception {
        String url = model.getConfig().getFirst(MyUserStorageProviderFactory.DATABASE_URL);
        String user = model.getConfig().getFirst(MyUserStorageProviderFactory.DATABASE_USER);
        String password = model.getConfig().getFirst(MyUserStorageProviderFactory.DATABASE_PASSWORD);

        logger.infof("Connecting to DB URL: %s with user: %s", url, user);
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
        logger.infof("Validating credentials for user %s", user.getUsername());
        if (!supportsCredentialType(input.getType())) {
            logger.info("Unsupported credential type: " + input.getType());
            return false;
        }

        try (Connection connection = getConnection();
             PreparedStatement stmt = connection.prepareStatement("SELECT password FROM users WHERE username = ?")) {

            stmt.setString(1, user.getUsername());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String storedPassword = rs.getString("password");
                    logger.info("Comparing passwords");
                    return input.getChallengeResponse().equals(storedPassword); // Simple compare for testing
                } else {
                    logger.info("No password found for user");
                }
            }
        } catch (Exception e) {
            logger.error("Error validating credentials", e);
        }
        return false;
    }

    @Override
    public int getUsersCount(RealmModel realm) {
        logger.info("Getting users count");
        try (Connection connection = getConnection();
             PreparedStatement stmt = connection.prepareStatement("SELECT COUNT(*) FROM users")) {

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt(1);
                    logger.infof("Users count: %d", count);
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
        String search = params.getOrDefault(UserModel.INCLUDE_SERVICE_ACCOUNT, "");
        logger.infof("searchForUserStream called with search=%s, firstResult=%d, maxResults=%d", search, firstResult, maxResults);

        try (Connection connection = getConnection();
             PreparedStatement stmt = connection.prepareStatement(
                     "SELECT * FROM users WHERE username LIKE ? OR email LIKE ? LIMIT ? OFFSET ?")) {

            stmt.setString(1, "%c%");
            stmt.setString(2, "%c%");
            stmt.setInt(3, maxResults == null ? 10 : maxResults);
            stmt.setInt(4, firstResult == null ? 0 : firstResult);

            ResultSet rs = stmt.executeQuery();
            logger.info("Query executed, mapping ResultSet to Stream<UserModel>");
            return mapToUserModelStream(realm, rs);

        } catch (Exception e) {
            logger.error("Error searching for users", e);
            return Stream.empty();
        }
    }

    private Stream<UserModel> mapToUserModelStream(RealmModel realm, ResultSet rs) {
        logger.info("mapToUserModelStream called");
        List<UserModel> list = new ArrayList<>();
        try {
            while (rs.next()) {
                // Log each column value for debugging
                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();
                StringBuilder rowLog = new StringBuilder("Row: ");

                for (int i = 1; i <= columnCount; i++) { // Column index starts at 1
                    String columnName = metaData.getColumnName(i);
                    Object value = rs.getObject(i); // Get the column value
                    rowLog.append(String.format("%s=%s, ", columnName, value));
                }

                logger.info(rowLog.toString());

                // Add the user model to the list
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
        logger.info("getGroupMembersStream called but not implemented");
        return Stream.empty();
    }

    @Override
    public Stream<UserModel> searchForUserByUserAttributeStream(RealmModel realm, String attrName, String attrValue) {
        logger.info("searchForUserByUserAttributeStream called but not implemented");
        return Stream.empty();
    }
}
