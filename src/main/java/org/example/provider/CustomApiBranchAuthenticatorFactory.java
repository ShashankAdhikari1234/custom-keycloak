package org.example.provider;

import org.jboss.logging.Logger;
import org.keycloak.Config;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;

import java.util.Collections;
import java.util.List;

public class CustomApiBranchAuthenticatorFactory implements AuthenticatorFactory {

    private static final Logger logger = Logger.getLogger(CustomApiBranchAuthenticatorFactory.class);
    public static final String PROVIDER_ID = "branch-auth";

    // Singleton instance
    private static final Authenticator SINGLETON = new CustomDirectGrantAuthenticator();

    // Only allow REQUIRED or DISABLED in the flow
    private static final AuthenticationExecutionModel.Requirement[] REQUIREMENT_CHOICES = {
            AuthenticationExecutionModel.Requirement.REQUIRED,
            AuthenticationExecutionModel.Requirement.DISABLED
    };

    @Override
    public String getDisplayType() {
        return "Branch Authenticator";
    }

    @Override
    public String getReferenceCategory() {
        return "branch";
    }

    @Override
    public boolean isConfigurable() {
        return false;
    }

    @Override
    public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
        return REQUIREMENT_CHOICES;
    }

    @Override
    public boolean isUserSetupAllowed() {
        // returning false means no required user setup required
        return false;
    }

    @Override
    public String getHelpText() {
        return "Verifies the provided branch against the backend database.";
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return Collections.emptyList();
    }

    @Override
    public Authenticator create(KeycloakSession session) {
        logger.infof("CustomApiBranchAuthenticatorFactory#create() called, returning singleton instance");
        return SINGLETON;
    }

    @Override
    public void init(Config.Scope config) {
        // any startup init code
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
        // post-init code if needed
    }

    @Override
    public void close() {
        // clean up if necessary
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }
}
