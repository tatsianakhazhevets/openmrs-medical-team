package apiParts.skelethon.endpoints;

import apiParts.models.BaseModel;
import apiParts.models.EmptyRequest;
import apiParts.models.EmptyResponse;
import apiParts.models.auth.LoginAdminRequest;
import apiParts.models.auth.LoginAdminResponse;
import apiParts.models.encounter.*;
import apiParts.models.order.GetOrderResponse;
import apiParts.models.patient.*;
import apiParts.models.visit.CreateVisitRequest;
import apiParts.models.visit.CreateVisitResponse;
import apiParts.models.visit.GetVisitByUuidResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Endpoint {
    LOGIN_GET(
            "/session",
            LoginAdminRequest.class,
            LoginAdminResponse.class),

    LOGOUT_DELETE(
            "/session",
            BaseModel.class,
            BaseModel.class),

    PATIENT_POST(
            "/patient",
            CreatePatientRequest.class,
            CreatePatientResponse.class),

    PATIENT_GET(
            "/patient",
            BaseModel.class,
            GetPatientResponse.class),

    PATIENT_SEARCH_GET(
            "/patient",
            GetPatientRequest.class,
            PatientSearchResponse.class),

    PATIENT_DELETE(
            "/patient",
            BaseModel.class,
            BaseModel.class),

    IDENTIFIER_GET(
            "/idgen/identifiersource/8549f706-7e85-4c1d-9424-217d50a2988b/identifier",
            GetIdentifierRequest.class,
            GetIdentifierResponse.class),

    PATIENT_IDENTIFIER_POST(
            "/patient",
            PatientIdentifierRequest.class,
            PatientIdentifierResponse.class),

    PATIENT_IDENTIFIER_UPDATE(
            "/patient",
            PatientIdentifierRequest.class,
            PatientIdentifierResponse.class),

    PATIENT_IDENTIFIER_DELETE(
            "/patient",
            BaseModel.class,
            BaseModel.class),

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
            GetObsResponse.class),

    ORDER_GET(
            "/order",
            EmptyRequest.class,
            GetOrderResponse.class),

    VISIT_POST(
            "/visit",
            CreateVisitRequest.class,
            CreateVisitResponse.class),

    VISIT_GET(
            "/visit",
            EmptyRequest.class,
            GetVisitByUuidResponse.class),

    VISIT_DELETE(
        "/visit",
        EmptyRequest.class,
        GetVisitByUuidResponse.class
    );

    private final String url;
    private final Class<? extends BaseModel> requestModel;
    private final Class<? extends BaseModel> responseModel;
}