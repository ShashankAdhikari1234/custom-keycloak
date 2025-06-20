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
    private static final String BRANCH_ATTRIBUTE = "branch";

    @Override
    public void authenticate(AuthenticationFlowContext context) {
        MultivaluedMap<String, String> formParams = context.getHttpRequest().getDecodedFormParameters();

        String branch = formParams.getFirst(BRANCH_ATTRIBUTE);
        if (branch == null || branch.trim().isEmpty()) {
            context.failure(AuthenticationFlowError.INVALID_CREDENTIALS);
            return;
        }
        UserModel user = context.getUser();
        if (!CustomValidator.validateUserBranch(branch, user)) {
            logger.warnf("User [%s] branch validation failed. Username: %s",
                    user.getUsername(), user.getUsername());
            context.failure(AuthenticationFlowError.INVALID_CREDENTIALS);
            return;
        }
        logger.infof("User [%s] branch validation passed.", user.getUsername());
        context.success();
    }

    @Override
    public void action(AuthenticationFlowContext context) {

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
