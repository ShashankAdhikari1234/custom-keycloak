package org.example.provider;


import org.example.config.DbConnectorConfig;
import org.example.utils.KeyCloakUtils;
import org.jboss.logging.Logger;
import org.keycloak.credential.CredentialInput;
import org.keycloak.credential.CredentialInputValidator;
import org.keycloak.models.GroupModel;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.credential.PasswordCredentialModel;
import org.keycloak.storage.StorageId;
import org.keycloak.storage.UserStorageProvider;
import org.keycloak.storage.user.UserLookupProvider;
import org.keycloak.storage.user.UserQueryProvider;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;


public class CustomUserStorageProvider
        implements UserStorageProvider, UserLookupProvider, CredentialInputValidator, UserQueryProvider {


    private final DbConnectorConfig dbConnectorConfig;

    private final KeyCloakUtils cloakUtils;


    private static final Logger logger = Logger.getLogger(CustomUserStorageProvider.class);

    public CustomUserStorageProvider(DbConnectorConfig dbConnectorConfig, KeyCloakUtils cloakUtils) {
        this.dbConnectorConfig = dbConnectorConfig;
        this.cloakUtils = cloakUtils;
    }


    @Override
    public UserModel getUserByUsername(RealmModel realm, String username) {
        logger.infof("getUserByUsername called - Realm: %s, Username: %s", realm.getName(), username);
        return cloakUtils.fetchUserFromDatabase(realm, "username", username);
    }


    @Override
    public UserModel getUserById(RealmModel realm, String storageId) {
        StorageId sid = new StorageId(storageId);
        return getUserByUsername(realm, sid.getExternalId());
    }

    @Override
    public UserModel getUserByEmail(RealmModel realm, String email) {
        logger.infof("getUserByEmail called - Realm: %s, Email: %s", realm.getName(), email);
        return cloakUtils.fetchUserFromDatabase(realm, "email", email);
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
        if (!supportsCredentialType(input.getType())) return false;
        String username = user.getUsername();
        String provided = input.getChallengeResponse();
        try (Connection c = dbConnectorConfig.getConnection();
             PreparedStatement st = c.prepareStatement(
                     "SELECT password FROM users WHERE username = ?")) {
            st.setString(1, username);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    String hash = rs.getString("password");
                    return verify(hash, provided);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return false;
    }

    private boolean verify(String storedHash, String plain) {
        return storedHash.equals(hash(plain));
    }

    private String hash(String plain) {
        return plain; // placeholder
    }

    @Override
    public Stream<UserModel> searchForUserStream(RealmModel realm, Map<String, String> params, Integer firstResult, Integer maxResults) {
        String search = params.getOrDefault(UserModel.INCLUDE_SERVICE_ACCOUNT, "");
        logger.infof("searchForUserStream called with search=%s, firstResult=%d, maxResults=%d", search, firstResult, maxResults);

        try (Connection connection = dbConnectorConfig.getConnection();
             PreparedStatement stmt = connection.prepareStatement(
                     "SELECT * FROM users")) {
            ResultSet rs = stmt.executeQuery();
            logger.info("Query executed, mapping ResultSet to Stream<UserModel>");
            return mapToUserModelStream(realm, rs);

        } catch (Exception e) {
            logger.error("Error searching for users", e);
            return Stream.empty();
        }
    }

    @Override
    public Stream<UserModel> getGroupMembersStream(RealmModel realm, GroupModel group, Integer firstResult, Integer maxResults) {
        return Stream.empty();
    }

    @Override
    public Stream<UserModel> searchForUserByUserAttributeStream(RealmModel realm, String attrName, String attrValue) {
        return Stream.empty();
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
                list.add(cloakUtils.mapToUserModel(realm, rs));
            }
        } catch (Exception e) {
            logger.error("Error mapping users", e);
            throw new RuntimeException(e);
        }
        return list.stream();
    }


    @Override
    public void close() { /* no-op */ }
}
