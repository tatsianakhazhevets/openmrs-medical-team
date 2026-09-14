package apiParts.skelethon.interfaces;

import apiParts.models.auth.LoginAdminRequest;

public interface AuthEndpoint {
    Object login(LoginAdminRequest loginAdminRequest);
}