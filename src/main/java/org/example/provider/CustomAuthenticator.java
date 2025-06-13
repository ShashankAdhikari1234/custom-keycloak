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

public class CustomAuthenticator implements Authenticator {

    private static final Logger logger = Logger.getLogger(CustomAuthenticator.class);

    @Override
    public void authenticate(AuthenticationFlowContext context) {
        logger.info("CustomAuthenticator: presenting branch form");
        String clientId = context.getAuthenticationSession().getClient().getClientId();
        String clientSecret = context.getAuthenticationSession().getClient().getSecret();
        logger.debugf("Client ID: %s", clientId);

        Response challenge = context.form()
                .setAttribute("client_id",clientId)
                .setAttribute("client_secret",clientSecret)
                .createForm("theme/mytheme/login/login.ftl");
        context.challenge(challenge);
    }

    @Override
    public void action(AuthenticationFlowContext context) {
        MultivaluedMap<String, String> form = context.getHttpRequest().getDecodedFormParameters();
        String branch = form.getFirst("branch");
        logger.infof("CustomAuthenticator: submitted branch=%s", branch);

        if (branch == null || !validateBranchFromDb(branch)) {
            logger.warn("Invalid branch: " + branch);
            Response challenge = context.form()
                    .setError("Invalid branch")
                    .createForm("login-branch.ftl");
            context.failureChallenge(AuthenticationFlowError.INVALID_CREDENTIALS, challenge);
            return;
        }

        context.success();
    }

    private boolean validateBranchFromDb(String branch) {
        // Replace with real DB fetch
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
        // No additional actions
    }

    @Override
    public void close() {
        // Nothing to clean up
    }
}
