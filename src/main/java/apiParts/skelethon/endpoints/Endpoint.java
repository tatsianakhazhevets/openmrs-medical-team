package apiParts.skelethon.endpoints;

import apiParts.models.BaseModel;
import apiParts.models.allergy.AllergyRequest;
import apiParts.models.allergy.AllergyResponse;
import apiParts.models.appointment.*;
import apiParts.models.auth.LoginAdminRequest;
import apiParts.models.auth.LoginAdminResponse;
import apiParts.models.encounter.*;
import apiParts.models.order.DiscontinueOrderRequest;
import apiParts.models.order.FulfillerDetailsRequest;
import apiParts.models.order.GetOrderResponse;
import apiParts.models.order.ListOrdersResponse;
import apiParts.models.order.Order;
import apiParts.models.patient.*;
import apiParts.models.procedure.CreateProcedureRequest;
import apiParts.models.procedure.GetProceduresResponse;
import apiParts.models.procedure.ProcedureResponse;
import apiParts.models.queue.GetQueueResponse;
import apiParts.models.queueEntry.CreateQueueEntryRequest;
import apiParts.models.queueEntry.EndQueueEntryRequest;
import apiParts.models.queueEntry.GetQueueEntryResponse;
import apiParts.models.queueEntry.QueueEntryResponse;
import apiParts.models.visit.CreateVisitRequest;
import apiParts.models.visit.CreateVisitResponse;
import apiParts.models.visit.GetVisitResponse;
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

    ENCOUNTER_POST(
            "/encounter",
            CreateEncounterRequest.class,
            CreateEncounterResponse.class),

    ENCOUNTER_GET(
            "/encounter",
            BaseModel.class,
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

    // POST /order: action=DISCONTINUE (see apiParts.models.order.DiscontinueOrderRequest)
    ORDER_POST(
            "/order",
            DiscontinueOrderRequest.class,
            Order.class),

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
            GetVisitResponse.class),

    VISIT_DELETE(
            "/visit",
            BaseModel.class,
            GetVisitResponse.class),

    VISIT_QUEUE_ENTRY_POST(
            "/visit-queue-entry",
            CreateQueueEntryRequest.class,
            QueueEntryResponse.class),

    QUEUE_ENTRY_UPDATE(
            "/queue-entry",
            EndQueueEntryRequest.class,
            QueueEntryResponse.class),

    QUEUE_ENTRY_GET(
            "/queue-entry",
            BaseModel.class,
            GetQueueEntryResponse.class),

    QUEUE_GET(
            "/queue",
            BaseModel.class,
            GetQueueResponse.class),

    APPOINTMENT_POST(
            "/appointment",
            CreateAppointmentRequest.class,
            CreateAppointmentResponse.class),

    APPOINTMENTS_SEARCH(
            "/appointments/search",
            AppointmentSearchRequest.class,
            CreateAppointmentResponse.class),

    // Nested CRUD (NestedCrudRequester): {parentUuid} is a placeholder for the owner uuid,
    // filled in via pathParam. One constant per resource: the HTTP method is chosen by the
    // method being called, so separate _POST/_UPDATE/_DELETE constants are not needed.
    PATIENT_ALLERGY_NESTED(
            "/patient/{parentUuid}/allergy",
            AllergyRequest.class,
            AllergyResponse.class),

    PATIENT_IDENTIFIER_NESTED(
            "/patient/{parentUuid}/identifier",
            PatientIdentifierRequest.class,
            PatientIdentifierResponse.class),

    // Flat CRUD for encounter (CrudRequester) - one constant for create/get/update/delete.
    ENCOUNTER_CRUD(
            "/encounter",
            CreateEncounterRequest.class,
            CreateEncounterResponse.class),

    // Action endpoints (ActionRequester): {resourceUuid} is the resource the command is performed on.
    APPOINTMENT_CHANGE_STATUS(
            "/appointments/{resourceUuid}/status-change",
            AppointmentStatusChangeRequest.class,
            CreateAppointmentResponse.class),

    // trailing slash kept on purpose: the tests were written and verified against it
    ORDER_FULFILLER_DETAILS(
            "/order/{resourceUuid}/fulfillerdetails/",
            FulfillerDetailsRequest.class,
            BaseModel.class);

    private final String url;
    private final Class<? extends BaseModel> requestModel;
    private final Class<? extends BaseModel> responseModel;
}