package org.example.utils;

/*
 * @Created At 18/06/2025
 * @Author ashim.gotame
 */

import org.jboss.logging.Logger;
import org.keycloak.models.UserModel;

import java.util.List;

public class CustomValidator {

    private static final Logger logger = Logger.getLogger(CustomValidator.class);


    public static boolean validateUserBranch(String expectedBranch, UserModel userDetail) {
        logger.info("Validating user branch...");

        // Retrieve the 'branch' attribute from the user's attributes
        List<String> branches = userDetail.getAttributes().get("branch");

        // Check if the 'branch' attribute exists and contains the expected value
        if (branches != null && !branches.isEmpty()) {
            String userBranch = branches.getFirst(); // Assuming 'branch' is a single-valued attribute
            logger.infof("User's branch: %s, Expected branch: %s", userBranch, expectedBranch);
            return expectedBranch.equals(userBranch);
        }

        // Log and return false if the 'branch' attribute is not found or is empty
        logger.warn("Branch attribute not found or is empty.");
        return false;
    }
}
