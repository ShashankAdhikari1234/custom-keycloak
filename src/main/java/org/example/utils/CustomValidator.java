package org.example.utils;

/*
 * @Created At 18/06/2025
 * @Author ashim.gotame
 */

import org.jboss.logging.Logger;
import org.keycloak.models.UserModel;

import java.util.Collections;
import java.util.List;

public class CustomValidator {

    private static final Logger logger = Logger.getLogger(CustomValidator.class);
    private static final String BRANCH_ATTRIBUTE = "branch";


    public static boolean validateUserBranch(String branch, UserModel userModel) {
        logger.info("Validating user branch...");
        List<String> branchList = getUserBranchAttributes(userModel);
        if (branchList.isEmpty()) {
            return false;
        }
        return branchList.stream().anyMatch(e -> e.equals(branch));
    }

    private static List<String> getUserBranchAttributes(UserModel user) {
        return user.getAttributes().getOrDefault(BRANCH_ATTRIBUTE, Collections.emptyList());
    }
}
