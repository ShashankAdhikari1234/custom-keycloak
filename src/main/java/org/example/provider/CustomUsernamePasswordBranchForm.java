// src/main/java/org/example/provider/CustomUsernamePasswordBranchForm.java
package org.example.provider;

import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.authenticators.browser.UsernamePasswordForm;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.utils.MediaType;

/**
 * Extends the default username/password form to add a "branch" field check.
 */
public class CustomUsernamePasswordBranchForm extends UsernamePasswordForm implements Authenticator {
    private static final Logger logger = Logger.getLogger(CustomUsernamePasswordBranchForm.class);
    private static final String BRANCH = "branch";
    private static final String VALID = "HO";

    @Override
    protected Response challenge(AuthenticationFlowContext context, String error, String field) {
        setBranchPlaceholder(context);
        return super.challenge(context, error, field);
    }

    @Override
    protected Response challenge(AuthenticationFlowContext context, MultivaluedMap<String, String> formData) {
        setBranchPlaceholder(context);
        return super.challenge(context, formData);
    }

    @Override
    protected boolean validateForm(AuthenticationFlowContext context, MultivaluedMap<String, String> formData) {
        boolean ok = super.validateForm(context, formData);
        if (!ok) return false;

        String branch = trim(formData.getFirst(BRANCH));
        logger.info("Branch entered: " + branch);
        if (branch.isEmpty()) {
            logger.info("Valid state empty check inside");
            return failJson(context, Response.Status.BAD_REQUEST, "error.branch_required", "Branch is required");
        }
        logger.info("Valid state outside");
        if (!VALID.equalsIgnoreCase(branch)) {
            logger.info("Valid state entered");
            return failJson(context, Response.Status.BAD_REQUEST, "error.invalid_branch", "Invalid Branch");
        }
        return true;
    }

    private boolean failJson(AuthenticationFlowContext context, Response.Status status, String errorKey, String fallbackMsg) {
        String localizedMsg = context.form().getMessage(errorKey);
        String message = localizedMsg != null ? localizedMsg : fallbackMsg;

        Response jsonResponse = Response
                .status(status)
                .type(MediaType.APPLICATION_JSON)
                .entity("{\"msg\": \"" + message + "\"}")
                .build();

        context.failureChallenge(AuthenticationFlowError.INVALID_CREDENTIALS, jsonResponse);
        return false;
    }


    private void setBranchPlaceholder(AuthenticationFlowContext ctx) {
        LoginFormsProvider form = ctx.form();
        form.setAttribute(BRANCH, "");
    }

    private String trim(String val) {
        return val == null ? "" : val.trim();
    }
}
