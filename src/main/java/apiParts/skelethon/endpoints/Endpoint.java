package apiParts.skelethon.endpoints;

import apiParts.models.BaseModel;
import apiParts.models.EmptyRequest;
import apiParts.models.EmptyResponse;
import apiParts.models.allergy.AllergyRequest;
import apiParts.models.allergy.AllergyResponse;
import apiParts.models.auth.LoginAdminRequest;
import apiParts.models.auth.LoginAdminResponse;
import apiParts.models.encounter.*;
import apiParts.models.order.GetOrderResponse;
import apiParts.models.order.ListOrdersResponse;
import apiParts.models.patient.*;
import apiParts.models.procedure.CreateProcedureRequest;
import apiParts.models.procedure.GetProceduresResponse;
import apiParts.models.procedure.ProcedureResponse;
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
            LoginAdminResponse.class
    ),

    LOGOUT_DELETE(
            "/session",
            BaseModel.class,
            BaseModel.class),
    PATIENT_POST(
            "/patient",
            CreatePatientRequest.class,
            CreatePatientResponse.class),
    LIST_ORDERS_GET(
            "/order",
            BaseModel.class,
            ListOrdersResponse.class),

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

    ENCOUNTER_GET(
            "/encounter",
            BaseModel.class,
            CreateEncounterResponse.class),

    ENCOUNTER_UPDATE(
            "/encounter/{uuid}",
            CreateEncounterRequest.class,
            CreateEncounterResponse.class),

    ENCOUNTER_DELETE(
            "/encounter",
            BaseModel.class,
            BaseModel.class),

    OBS_DELETE(
            "/obs",
            BaseModel.class,
            BaseModel.class),

    OBS_GET(
            "/obs",
            GetObsRequest.class,
            GetObsResponse.class),

    ORDER_GET(
            "/order",
            BaseModel.class,
            GetOrderResponse.class),

    ORDER_DELETE(
            "/order",
            BaseModel.class,
            BaseModel.class),

    PROCEDURE_POST(
            "/procedure",
            CreateProcedureRequest.class,
            ProcedureResponse.class),

    PROCEDURE_GET(
            "/procedure",
            BaseModel.class,
            ProcedureResponse.class),

    // POST /procedure/{uuid}: partial update, only sent fields are changed
    PROCEDURE_UPDATE(
            "/procedure",
            CreateProcedureRequest.class,
            ProcedureResponse.class),

    // DELETE /procedure/{uuid} -> void
    PROCEDURE_DELETE(
            "/procedure",
            BaseModel.class,
            BaseModel.class),

    // GET /procedure?patient={uuid}&v=full[&includeAll=true]
    PROCEDURES_GET(
            "/procedure",
            BaseModel.class,
            GetProceduresResponse.class),

    VISIT_POST(
            "/visit",
            CreateVisitRequest.class,
            CreateVisitResponse.class),

    VISIT_GET(
            "/visit",
            BaseModel.class,
            GetVisitByUuidResponse.class),

    VISIT_DELETE(
        "/visit",
        EmptyRequest.class,
        GetVisitByUuidResponse.class),

    ALLERGY_POST(
            "/allergy",
            AllergyRequest.class,
            AllergyResponse.class),

    PATIENT_ALLERGY_GET(
            "/allergy",
            BaseModel.class,
            AllergyResponse.class
    ),

    ALLERGY_UPDATE(
            "/allergy",
            AllergyRequest.class,
            AllergyResponse.class),

    ALLERGY_DELETE(
            "/allergy",
            BaseModel.class,
            AllergyResponse.class);
        BaseModel.class,
        GetVisitByUuidResponse.class
    );

    private final String url;
    private final Class<? extends BaseModel> requestModel;
    private final Class<? extends BaseModel> responseModel;
}