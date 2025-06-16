package org.example.provider;

import jakarta.ws.rs.core.MultivaluedMap;
import org.jboss.logging.Logger;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.Authenticator;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import jakarta.ws.rs.core.Response;

import java.util.Objects;

public class CustomDirectGrantAuthenticator implements Authenticator {

    private static final Logger logger = Logger.getLogger(CustomDirectGrantAuthenticator.class);

    @Override
    public void authenticate(AuthenticationFlowContext context) {
        MultivaluedMap<String, String> formParams = context.getHttpRequest().getDecodedFormParameters();
        String branch = formParams.getFirst("branch");
        logger.info("Branch"+ branch);
        if (branch == null || branch.trim().isEmpty()) {
            context.failure(AuthenticationFlowError.INVALID_CREDENTIALS);
            return;
        }
        if (!validateBranchFromDb(branch)) {
            context.failure(AuthenticationFlowError.INVALID_CREDENTIALS);
            return;
        }
        context.success();
    }


    @Override
    public void action(AuthenticationFlowContext context) {
        MultivaluedMap<String, String> formParams = context.getHttpRequest().getDecodedFormParameters();
        String branch = formParams.getFirst("branch");

        if (branch == null || branch.trim().isEmpty()) {
            logger.warn("Branch is null or empty in request.");
            Response error = Response.status(Response.Status.BAD_REQUEST)
                    .entity("Branch is required")
                    .type("text/plain")
                    .build();
            context.failureChallenge(AuthenticationFlowError.INVALID_CREDENTIALS, error);
            return;
        }

        logger.infof("Validating submitted branch: %s", branch);

        if (!validateBranchFromDb(branch)) {
            logger.warnf("Invalid branch submitted: %s", branch);
            Response error = Response.status(Response.Status.UNAUTHORIZED)
                    .entity("Invalid branch")
                    .type("text/plain")
                    .build();
            context.failureChallenge(AuthenticationFlowError.INVALID_CREDENTIALS, error);
            return;
        }

        logger.info("Branch validation successful.");
        context.success();
    }

    private boolean validateBranchFromDb(String branch) {
        logger.info("Branch :" +branch);
        return Objects.equals(branch, "HO");
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
        // No required actions
    }

    @Override
    public void close() {
        // No cleanup needed
    }
}
