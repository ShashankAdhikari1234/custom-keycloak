package org.example.provider;

import jakarta.ws.rs.core.MultivaluedMap;
import org.jboss.logging.Logger;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.Authenticator;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

import java.util.Objects;

/**
 * Custom authenticator that adds a "branch" field validation to the login flow.
 */
public class CustomBrowserAuthenticator implements Authenticator {

    private static final Logger logger = Logger.getLogger(CustomBrowserAuthenticator.class);
    private static final String LOGIN_FORM = "customLogin";
    private static final String VALID_BRANCH = "HO";

    @Override
    public void authenticate(AuthenticationFlowContext context) {
        logger.info("Rendering custom login form with branch field...");

        MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();
        String username = getValueOrEmpty(formData.getFirst("username"));
        String branch = getValueOrEmpty(formData.getFirst("branch"));
        logger.info("username:"+ username + " " + "branch :" + branch);
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

    private boolean validateBranchFromDb(String branch) {
        logger.info("Branch :" +branch);
        return Objects.equals(branch, "HO");
    }

    @Override
    public void action(AuthenticationFlowContext context) {
        MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();
        String username = getValueOrEmpty(formData.getFirst("username"));
        String branch = getValueOrEmpty(formData.getFirst("branch"));

        if (branch.isEmpty()) {
            showError(context, "Branch is required", username, branch);
            return;
        }

        if (!VALID_BRANCH.equalsIgnoreCase(branch)) {
            showError(context, "Invalid branch", username, branch);
            return;
        }

        context.success(); // Validation successful
    }

    @Override
    public boolean requiresUser() {
        return false;
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

    /**
     * Utility to get non-null trimmed string or empty string.
     */
    private String getValueOrEmpty(String value) {
        return value != null ? value.trim() : "";
    }

    /**
     * Helper to return a failed login attempt with a user-friendly error message.
     */
    private void showError(AuthenticationFlowContext context, String errorMessage, String username, String branch) {
        context.failureChallenge(
                AuthenticationFlowError.INVALID_CREDENTIALS,
                context.form()
                        .setError(errorMessage)
                        .setAttribute("username", username)
                        .setAttribute("branch", branch)
                        .createForm(LOGIN_FORM)
        );
    }
}
