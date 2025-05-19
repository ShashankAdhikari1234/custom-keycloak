package org.example;

import org.keycloak.models.RealmModel;
import org.keycloak.models.SubjectCredentialManager;
import org.keycloak.storage.adapter.AbstractUserAdapter;

public class FederatedUserAdapter extends AbstractUserAdapter {

    private final String id;
    private String username;
    private String email;

    public FederatedUserAdapter(RealmModel realm, String id) {
        super(null, realm, null);
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String getEmail() {
        return email;
    }

    @Override
    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public SubjectCredentialManager credentialManager() {
        return null;
    }

    // Add any additional user attributes or methods you need
}
