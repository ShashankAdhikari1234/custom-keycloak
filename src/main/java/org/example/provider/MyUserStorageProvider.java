package org.example.provider;

import org.keycloak.component.ComponentModel;
import org.keycloak.credential.CredentialInput;
import org.keycloak.credential.CredentialInputValidator;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.credential.PasswordCredentialModel;
import org.keycloak.storage.UserStorageProvider;
import org.keycloak.storage.adapter.AbstractUserAdapterFederatedStorage;
import org.keycloak.storage.user.UserLookupProvider;
import org.jboss.logging.Logger;

/**
 * @author sashank.adhikari on 5/6/2025
 */
public class MyUserStorageProvider implements UserStorageProvider, UserLookupProvider, CredentialInputValidator {

    private static final Logger logger = Logger.getLogger(MyUserStorageProvider.class);

    private final KeycloakSession session;
    private final ComponentModel model;

    private static final String HARDCODED_USERNAME = "external-user";
    private static final String HARDCODED_PASSWORD = "password123";
    private static final String HARDCODED_EMAIL = "external@example.com";

    public MyUserStorageProvider(KeycloakSession session, ComponentModel model) {
        this.session = session;
        this.model = model;
    }

    @Override
    public void close() {
        // Nothing to close
    }

    @Override
    public UserModel getUserByUsername(RealmModel realm, String username) {
        logger.infof("Called getUserByUsername with: %s", username);
        logger.infof("-------------------------------Called getUserByUsername with: %s-----------------------------------------------------------------------------", username);
        if (!HARDCODED_USERNAME.equalsIgnoreCase(username)) return null;
        return createVirtualUser(realm);
    }

    @Override
    public UserModel getUserById(RealmModel realm, String id) {
        // You can choose any logic here to map the ID to a unique user.
        String expectedId = model.getId() + "::" + HARDCODED_USERNAME;
        if (!expectedId.equals(id)) return null;
        return createVirtualUser(realm);
    }

    @Override
    public UserModel getUserByEmail(RealmModel realm, String email) {
        if (HARDCODED_EMAIL.equalsIgnoreCase(email)) {
            return createVirtualUser(realm);
        }
        return null;
    }

    private UserModel createVirtualUser(RealmModel realm) {
        // Here we use a custom implementation to handle the user ID manually.
        AbstractUserAdapterFederatedStorage user = new AbstractUserAdapterFederatedStorage(session, realm, model) {
            @Override
            public String getUsername() {
                return HARDCODED_USERNAME;
            }

            @Override
            public void setUsername(String username) {
                // no-op
            }

            @Override
            public String getId() {
                return model.getId() + "::" + HARDCODED_USERNAME;  // Unique ID logic
            }
        };
        user.setEmail(HARDCODED_EMAIL);
        user.setEnabled(true);
        user.setFirstName("External");
        user.setLastName("User");
        return user;
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
        logger.infof("Validating credentials for: %s with password: %s", user.getUsername(), input.getChallengeResponse());

        if (!supportsCredentialType(input.getType())) return false;
        String inputPassword = input.getChallengeResponse();
        return HARDCODED_USERNAME.equalsIgnoreCase(user.getUsername())
                && HARDCODED_PASSWORD.equals(inputPassword);
    }
}
