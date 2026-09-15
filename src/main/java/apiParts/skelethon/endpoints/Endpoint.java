package apiParts.skelethon.endpoints;

import apiParts.models.BaseModel;
import apiParts.models.EmptyRequest;
import apiParts.models.EmptyResponse;
import apiParts.models.auth.LoginAdminRequest;
import apiParts.models.auth.LoginAdminResponse;
import apiParts.models.encounter.*;
import apiParts.models.patient.*;
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

    IDENTIFIER_GET(
            "/idgen/identifiersource/8549f706-7e85-4c1d-9424-217d50a2988b/identifier",
            GetIdentifierRequest.class,
            GetIdentifierResponse.class),

    ENCOUNTER_POST(
            "/encounter",
            CreateEncounterRequest.class,
            CreateEncounterResponse.class),

    ENCOUNTER_DELETE(
            "/encounter",
            EmptyRequest.class,
            EmptyResponse.class),

    OBS_DELETE(
            "/obs",
            EmptyRequest.class,
            EmptyResponse.class),

    OBS_GET(
            "/obs",
            GetObsRequest.class,
            GetObsResponse.class
    );

    private final String url;
    private final Class<? extends BaseModel> requestModel;
    private final Class<? extends BaseModel> responseModel;
}