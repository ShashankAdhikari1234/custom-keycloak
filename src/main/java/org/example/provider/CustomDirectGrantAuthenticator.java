package org.example.provider;

import jakarta.ws.rs.core.MultivaluedMap;
import org.example.utils.CustomValidator;
import org.jboss.logging.Logger;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.Authenticator;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

import java.util.Collections;
import java.util.List;

public class CustomDirectGrantAuthenticator implements Authenticator {

    private static final Logger logger = Logger.getLogger(CustomDirectGrantAuthenticator.class);
    private static final String REQUIRED_BRANCH = "HO";
    private static final String BRANCH_ATTRIBUTE = "branch";

    @Override
    public void authenticate(AuthenticationFlowContext context) {
        UserModel user = context.getUser();
        logger.info("User Model :"+user);
        List<String> branches = getUserBranchAttributes(user);

        logger.infof("Authenticating user [%s] with branches: %s", user.getUsername(), branches);

        if (branches.isEmpty()) {
            logger.warnf("User [%s] has no branch attribute.", user.getUsername());
            context.failure(AuthenticationFlowError.INVALID_CREDENTIALS);
            return;
        }

        if (!CustomValidator.validateUserBranch(REQUIRED_BRANCH, branches)) {
            logger.warnf("User [%s] branch validation failed. Required: %s, Found: %s",
                    user.getUsername(), REQUIRED_BRANCH, branches);
            context.failure(AuthenticationFlowError.INVALID_CREDENTIALS);
            return;
        }

        logger.infof("User [%s] branch validation passed.", user.getUsername());
        context.success(); // ✅ Use success for Direct Grant
    }

    @Override
    public void action(AuthenticationFlowContext context) {

    }

    private List<String> getUserBranchAttributes(UserModel user) {
        return user.getAttributes().getOrDefault(BRANCH_ATTRIBUTE, Collections.emptyList());
    }

    @Override
    public boolean requiresUser() {
        return true;
    }

    @Override
    public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
        return true;
    }

    @Override
    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {
        // No required actions for Direct Grant
    }

    @Override
    public void close() {
        // No resources to clean up
    }
}
