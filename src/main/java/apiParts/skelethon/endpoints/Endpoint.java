package apiParts.skelethon.endpoints;

import apiParts.models.BaseModel;
import apiParts.models.auth.LoginAdminRequest;
import apiParts.models.auth.LoginAdminResponse;
import apiParts.models.patient.CreatePatientRequest;
import apiParts.models.patient.CreatePatientResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Endpoint {
    LOGIN_GET(
            "/session",
            LoginAdminRequest.class,
            LoginAdminResponse.class),

    PATIENT_POST(
            "/patient",
            CreatePatientRequest.class,
            CreatePatientResponse.class),


    ;

    private final String url;
    private final Class<? extends BaseModel> requestModel;
    private final Class<? extends BaseModel> responseModel;
}