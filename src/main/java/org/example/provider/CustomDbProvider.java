package org.example.provider;

import org.jboss.logging.Logger;
import org.keycloak.component.ComponentModel;
import org.keycloak.credential.CredentialInput;
import org.keycloak.credential.CredentialInputValidator;
import org.keycloak.models.*;
import org.keycloak.models.credential.PasswordCredentialModel;
import org.keycloak.storage.UserStorageProvider;
import org.keycloak.storage.adapter.AbstractUserAdapterFederatedStorage;
import org.keycloak.storage.user.UserLookupProvider;
import org.keycloak.storage.user.UserQueryProvider;

import java.sql.*;
import java.util.*;
import java.util.stream.Stream;

public class CustomDbProvider implements UserStorageProvider, UserLookupProvider, CredentialInputValidator, UserQueryProvider {

    private static final Logger logger = Logger.getLogger(CustomDbProvider.class);
    private final KeycloakSession session;
    private final ComponentModel model;

    private static final String SQL_SELECT_USER_BY = "SELECT * FROM users WHERE %s = ?";
    private static final String SQL_SELECT_ALL_USERS = "SELECT * FROM users";
    private static final String SQL_SELECT_USER_COUNT = "SELECT COUNT(*) FROM users";
    private static final String SQL_SELECT_PASSWORD = "SELECT password FROM users WHERE username = ?";

    public CustomDbProvider(KeycloakSession session, ComponentModel model) {
        this.session = session;
        this.model = model;
    }

    private Connection getConnection() throws SQLException {
        String url = model.getConfig().getFirst(CustomDBFederationProviderFactory.DATABASE_URL);
        String user = model.getConfig().getFirst(CustomDBFederationProviderFactory.DATABASE_USER);
        String password = model.getConfig().getFirst(CustomDBFederationProviderFactory.DATABASE_PASSWORD);
        return DriverManager.getConnection(url, user, password);
    }

    @Override
    public UserModel getUserByUsername(RealmModel realm, String username) {
        return fetchUserFromDatabase(realm, "username", username);
    }

    @Override
    public UserModel getUserById(RealmModel realm, String id) {
        String username = id.substring(id.lastIndexOf(':') + 1);
        return getUserByUsername(realm, username);
    }

    @Override
    public UserModel getUserByEmail(RealmModel realm, String email) {
        return fetchUserFromDatabase(realm, "email", email);
    }

    private UserModel fetchUserFromDatabase(RealmModel realm, String field, String value) {
        String query = String.format(SQL_SELECT_USER_BY, field);
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, value);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapToUserModel(realm, rs);
                }
            }
        } catch (Exception e) {
            logger.errorf(e, "Error fetching user by %s", field);
        }
        return null;
    }

    private String getSafe(ResultSet rs, String column) {
        try {
            return rs.getString(column);
        } catch (SQLException e) {
            return null;
        }
    }

    private UserModel mapToUserModel(RealmModel realm, ResultSet rs) {
        return new AbstractUserAdapterFederatedStorage(session, realm, model) {
            private final String username = getSafe(rs, "username");
            private String email = getSafe(rs, "email");
            private String firstName = getSafe(rs, "first_name");
            private String lastName = getSafe(rs, "last_name");

            @Override public String getUsername() { return username; }

            @Override
            public void setUsername(String username) {

            }

            @Override public String getEmail() { return email; }
            @Override public String getFirstName() { return firstName; }
            @Override public String getLastName() { return lastName; }

            @Override public void setEmail(String email) { this.email = email; }
            @Override public void setLastName(String lastName) { this.lastName = lastName; }

            @Override public Map<String, List<String>> getAttributes() {
                return getFederatedStorage().getAttributes(realm, getId());
            }

            @Override public void setAttribute(String name, List<String> values) {
                getFederatedStorage().setAttribute(realm, getId(), name, values);
            }
        };
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
        if (!supportsCredentialType(input.getType()) || !(input instanceof PasswordCredentialModel)) return false;
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(SQL_SELECT_PASSWORD)) {
            stmt.setString(1, user.getUsername());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String stored = rs.getString("password");
                    return input.getChallengeResponse().equals(stored); // replace with BCrypt.compare() in prod
                }
            }
        } catch (Exception e) {
            logger.error("Credential validation failed", e);
        }
        return false;
    }

    @Override
    public int getUsersCount(RealmModel realm) {
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(SQL_SELECT_USER_COUNT); ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) {
            logger.error("Failed to get user count", e);
        }
        return 0;
    }

    @Override
    public Stream<UserModel> searchForUserStream(RealmModel realm, Map<String, String> params, Integer first, Integer max) {
        String search = params.getOrDefault("search", "");
        String sql = "SELECT * FROM users WHERE username ILIKE ? OR email ILIKE ? LIMIT ? OFFSET ?";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "%" + search + "%");
            stmt.setString(2, "%" + search + "%");
            stmt.setInt(3, max != null ? max : 10);
            stmt.setInt(4, first != null ? first : 0);
            try (ResultSet rs = stmt.executeQuery()) {
                List<UserModel> users = new ArrayList<>();
                while (rs.next()) users.add(mapToUserModel(realm, rs));
                return users.stream();
            }
        } catch (Exception e) {
            logger.error("Failed to search users", e);
        }
        return Stream.empty();
    }

    @Override public Stream<UserModel> getGroupMembersStream(RealmModel realm, GroupModel group, Integer first, Integer max) { return Stream.empty(); }
    @Override public Stream<UserModel> searchForUserByUserAttributeStream(RealmModel realm, String attrName, String attrValue) { return Stream.empty(); }
    @Override public void close() { logger.info("CustomDbProvider closed"); }
}
