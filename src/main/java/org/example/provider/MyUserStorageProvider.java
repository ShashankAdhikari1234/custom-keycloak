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
        logger.info("MyUserStorageProvider initialized with model ID: " + model.getId());
    }

    @Override
    public void close() {
        logger.info("MyUserStorageProvider closing");
    }

    @Override
    public UserModel getUserByUsername(RealmModel realm, String username) {
        logger.infof("getUserByUsername called - Realm: %s, Username: %s", realm.getName(), username);
        
        if (!HARDCODED_USERNAME.equalsIgnoreCase(username)) {
            logger.infof("Username %s not found in our provider", username);
            return null;
        }
        
        logger.infof("Found user %s in our provider", username);
        return createVirtualUser(realm);
    }

    @Override
    public UserModel getUserById(RealmModel realm, String id) {
        logger.infof("getUserById called - Realm: %s, ID: %s", realm.getName(), id);
        
        String expectedId = model.getId() + "::" + HARDCODED_USERNAME;
        if (!expectedId.equals(id)) {
            logger.infof("User ID %s not found in our provider", id);
            return null;
        }
        
        logger.infof("Found user with ID %s in our provider", id);
        return createVirtualUser(realm);
    }

    @Override
    public UserModel getUserByEmail(RealmModel realm, String email) {
        logger.infof("getUserByEmail called - Realm: %s, Email: %s", realm.getName(), email);
        
        if (!HARDCODED_EMAIL.equalsIgnoreCase(email)) {
            logger.infof("Email %s not found in our provider", email);
            return null;
        }
        
        logger.infof("Found user with email %s in our provider", email);
        return createVirtualUser(realm);
    }

    private UserModel createVirtualUser(RealmModel realm) {
        logger.infof("Creating virtual user for realm: %s", realm.getName());
        
        AbstractUserAdapterFederatedStorage user = new AbstractUserAdapterFederatedStorage(session, realm, model) {
            @Override
            public String getUsername() {
                logger.infof("getUsername called, returning: %s", HARDCODED_USERNAME);
                return HARDCODED_USERNAME;
            }

            @Override
            public void setUsername(String username) {
                logger.infof("setUsername called with: %s (ignored)", username);
                // no-op
            }

            @Override
            public String getId() {
                String id = model.getId() + "::" + HARDCODED_USERNAME;
                logger.infof("getId called, returning: %s", id);
                return id;
            }
        };
        
        user.setEmail(HARDCODED_EMAIL);
        user.setEnabled(true);
        user.setFirstName("External");
        user.setLastName("User");
        
        logger.infof("Virtual user created with username: %s, email: %s", HARDCODED_USERNAME, HARDCODED_EMAIL);
        return user;
    }

    @Override
    public boolean supportsCredentialType(String credentialType) {
        logger.infof("supportsCredentialType called with: %s", credentialType);
        boolean supports = PasswordCredentialModel.TYPE.equals(credentialType);
        logger.infof("Credential type %s supported: %s", credentialType, supports);
        return supports;
    }

    @Override
    public boolean isConfiguredFor(RealmModel realm, UserModel user, String credentialType) {
        logger.infof("isConfiguredFor called - Realm: %s, User: %s, CredentialType: %s", 
            realm.getName(), user.getUsername(), credentialType);
        boolean configured = supportsCredentialType(credentialType);
        logger.infof("User %s configured for credential type %s: %s", 
            user.getUsername(), credentialType, configured);
        return configured;
    }

    @Override
    public boolean isValid(RealmModel realm, UserModel user, CredentialInput input) {
        logger.infof("isValid called - Realm: %s, User: %s, CredentialType: %s", 
            realm.getName(), user.getUsername(), input.getType());

        if (!supportsCredentialType(input.getType())) {
            logger.infof("Credential type %s not supported", input.getType());
            return false;
        }

        String inputPassword = input.getChallengeResponse();
        boolean valid = HARDCODED_USERNAME.equalsIgnoreCase(user.getUsername())
                && HARDCODED_PASSWORD.equals(inputPassword);
                
        logger.infof("Password validation for user %s: %s", user.getUsername(), valid);
        return valid;
    }
}
