package apiParts.skelethon.endpoints;

import apiParts.models.BaseModel;
import apiParts.models.auth.LoginAdminRequest;
import apiParts.models.auth.LoginAdminResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Endpoint {
    LOGIN_GET(
            "/session",
            LoginAdminRequest.class,
            LoginAdminResponse.class),


    ;

    private final String url;
    private final Class<? extends BaseModel> requestModel;
    private final Class<? extends BaseModel> responseModel;
}