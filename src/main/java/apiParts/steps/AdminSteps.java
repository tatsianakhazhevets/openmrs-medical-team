package apiParts.steps;

import apiParts.models.*;
import apiParts.models.appointment.*;
import apiParts.models.auth.LoginAdminRequest;
import apiParts.models.auth.LoginAdminResponse;
import apiParts.models.encounter.CreateEncounterRequest;
import apiParts.models.encounter.CreateEncounterResponse;
import apiParts.models.encounter.ObsResponse;
import apiParts.models.order.CareSetting;
import apiParts.models.order.DiscontinueOrderRequest;
import apiParts.models.order.Drug;
import apiParts.models.order.DrugOrder;
import apiParts.models.order.FulfillerStatus;
import apiParts.models.order.Order;
import apiParts.models.patient.*;
import apiParts.models.procedure.ProcedureResponse;
import apiParts.models.queue.*;
import apiParts.models.queueEntry.*;
import apiParts.models.visit.CreateVisitRequest;
import apiParts.models.visit.CreateVisitResponse;
import apiParts.models.visit.VisitType;
import apiParts.skelethon.endpoints.Endpoint;
import apiParts.skelethon.requests.auth.SuccessfulAuthRequester;
import apiParts.skelethon.requests.common.SuccessfulCrudRequester;
import apiParts.skelethon.requests.action.SuccessfulActionRequester;
import apiParts.skelethon.requests.action.ActionRequester;
import apiParts.models.encounter.ObsSearchParams;
import apiParts.models.order.OrderSearchParams;
import apiParts.models.order.DrugOrderResponse;
import apiParts.models.search.SearchResult;
import apiParts.models.search.SearchParams;
import apiParts.skelethon.requests.search.SuccessfulSearchRequester;
import apiParts.specs.RequestSpecs;
import apiParts.specs.ResponseSpecs;
import apiParts.testdata.AppointmentTestData;
import apiParts.testdata.OrderTestData;
import apiParts.testdata.PatientTestData;
import apiParts.testdata.ProcedureTestData;
import apiParts.testdata.QueueTestData;
import apiParts.testdata.VitalsTestData;

import java.util.List;
import java.util.Map;

public class AdminSteps {

    private static volatile String currentProviderUuid;

    // adminSpec() is authenticated by itself (Basic auth header), no login call is needed
    public static CreatePatientResponse createPatient() {
        LoginAdminRequest loginAdminRequest = LoginAdminRequest.builder()
                .username("admin")
                .password("Admin123")
                .build();

        new SuccessfulAuthRequester<LoginAdminResponse>(
                RequestSpecs.unAuthSpec(),
                Endpoint.LOGIN_GET,
                ResponseSpecs.requestReturnsOk())
                .login(loginAdminRequest);

        return new SuccessfulCrudRequester<CreatePatientResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.PATIENT_POST,
                ResponseSpecs.requestReturnsCreated())
                .create(PatientTestData.createPatientRequest(getId()));
    }

    public static CreateEncounterResponse createVitalsEncounter(String patientUUID) {
        CreateEncounterRequest createEncounterRequest = CreateEncounterRequest.builder()
                .patient(patientUUID)
                .encounterType(EncounterType.VITALS)
                .location(Location.OUTPATIENT_CLINIC)
                .obs(VitalsTestData.vitalsObs())
                .build();

        return new SuccessfulCrudRequester<CreateEncounterResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.ENCOUNTER_POST,
                ResponseSpecs.requestReturnsCreated())
                .create(createEncounterRequest);
    }

    public static CreateVisitResponse createVisitWithRequiredFields(String patientUUID) {
        CreateVisitRequest request = CreateVisitRequest.builder().patient(patientUUID)
                .visitType(VisitType.FACILITY_VISIT)
                .build();

        return new SuccessfulCrudRequester<CreateVisitResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.VISIT_POST,
                ResponseSpecs.requestReturnsCreated()).create(request);
    }

    // Valid outpatient drug order (Aspirin, simple dosing) as a standard fixture for order-related tests.
    // Request: OrderTestData.drugOrderEncounterRequest(patientUUID, ordererUUID)
    public static CreateEncounterResponse createDrugOrderEncounter(String patientUUID) {
        return new SuccessfulCrudRequester<CreateEncounterResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.ENCOUNTER_POST,
                ResponseSpecs.requestReturnsCreated())
                .create(OrderTestData.drugOrderEncounterRequest(patientUUID, getCurrentProviderUuid()));
    }

    // Request behind createDrugOrderEncounter, exposed so callers can build the expected
    // response model from it (see apiParts.assertions.OrderAssertions)
    public static CreateEncounterRequest drugOrderEncounterRequest(String patientUUID) {
        return OrderTestData.drugOrderEncounterRequest(patientUUID, getCurrentProviderUuid());
    }

    // Same standard outpatient drug order as drugOrderEncounterRequest, but scheduled ahead
    // (urgency=ON_SCHEDULED_DATE) - Upcoming Medications fixture. Exposed as a request (not create+request
    // pair) since callers need the same instance both to POST /encounter and to build the expected
    // response model (see apiParts.assertions.OrderAssertions) - scheduledDate is time-sensitive.
    public static CreateEncounterRequest upcomingDrugOrderEncounterRequest(String patientUUID) {
        return OrderTestData.upcomingDrugOrderEncounterRequest(patientUUID, getCurrentProviderUuid());
    }

    // Valid inpatient lab order (Alkaline phosphatase test) as a standard fixture for order-related tests.
    // Request: OrderTestData.labOrderEncounterRequest(patientUUID, ordererUUID)
    public static CreateEncounterResponse createLabOrderEncounter(String patientUUID) {
        return new SuccessfulCrudRequester<CreateEncounterResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.ENCOUNTER_POST,
                ResponseSpecs.requestReturnsCreated())
                .create(OrderTestData.labOrderEncounterRequest(patientUUID, getCurrentProviderUuid()));
    }

    // Valid procedure with required fields only as a standard fixture for procedure tests.
    // Request: ProcedureTestData.procedureRequest(patientUUID)
    public static ProcedureResponse createProcedure(String patientUUID) {
        return new SuccessfulCrudRequester<ProcedureResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.PROCEDURE_POST,
                ResponseSpecs.requestReturnsCreated())
                .create(ProcedureTestData.procedureRequest(patientUUID));
    }

    public static void deleteVisit(String visitUUID) {
        new SuccessfulCrudRequester<CreateVisitResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.VISIT_DELETE,
                ResponseSpecs.requestReturnsNoContent()
        ).delete(visitUUID, Map.of("purge", true));
    }

    public static QueueEntryResponse addPatientToQueue(String patientUUID, String visitUUID) {
        QueueResponse queue = getOutpatientConsultationQueue();

        return new SuccessfulCrudRequester<QueueEntryResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.VISIT_QUEUE_ENTRY_POST,
                ResponseSpecs.requestReturnsCreated()
        ).create(QueueTestData.queueEntryRequest(queue.getUuid(), visitUUID, patientUUID));
    }

    public static QueueEntryResponse updateQueueEntry(
            String queueEntryUUID,
            QueueStatus status,
            QueuePriority priority,
            String priorityComment) {

        return new SuccessfulCrudRequester<QueueEntryResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.QUEUE_ENTRY_UPDATE,
                ResponseSpecs.requestReturnsOk()
        ).update(queueEntryUUID, QueueTestData.updateQueueEntryRequest(status, priority, priorityComment));
    }

    public static QueueEntryResponse endQueueEntry(String queueEntryUUID) {
        return new SuccessfulCrudRequester<QueueEntryResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.QUEUE_ENTRY_UPDATE,
                ResponseSpecs.requestReturnsOk()
        ).update(queueEntryUUID, QueueTestData.endQueueEntryRequest());
    }

    public static SearchResult<QueueEntryResponse> getActiveQueueEntries() {
        return new SuccessfulSearchRequester<QueueEntryResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.QUEUE_ENTRY_GET,
                ResponseSpecs.requestReturnsOk()
        ).search(QueueEntrySearchParams.builder()
                .representation(QueueEntrySearchParams.ACTIVE_ENTRY_REPRESENTATION)
                .location(Location.OUTPATIENT_CLINIC.getUuid())
                .isEnded(false)
                .build());
    }

    public static QueueResponse getOutpatientConsultationQueue() {
        return new SuccessfulSearchRequester<QueueResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.QUEUE_GET,
                ResponseSpecs.requestReturnsOk()
        ).search(QueueSearchParams.builder()
                        .representation(QueueSearchParams.QUEUE_LOOKUP_REPRESENTATION)
                        .build())
                .requireOne(queue ->
                                QueueType.OUTPATIENT_CONSULTATION.getDisplay().equals(queue.getName())
                                        && Location.OUTPATIENT_CLINIC.getDisplay().equals(queue.getLocation().getDisplay()),
                        "Queue 'Outpatient Consultation' at 'Outpatient Clinic'");
    }

    public static CreateAppointmentResponse createAppointment(CreateAppointmentRequest request) {
        return new SuccessfulCrudRequester<CreateAppointmentResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.APPOINTMENT_POST,
                ResponseSpecs.requestReturnsOk()
        ).create(request);
    }

    public static CreateAppointmentResponse createAppointment(String patientUUID) {
        return createAppointment(AppointmentTestData.appointmentRequest(patientUUID));
    }

    public static CreateAppointmentResponse cancelAppointment(
            String appointmentUUID,
            AppointmentStatusChangeRequest request) {

        // POST /appointments/{uuid}/status-change - a command on the appointment, not CRUD
        return new SuccessfulActionRequester<CreateAppointmentResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.APPOINTMENT_CHANGE_STATUS,
                ResponseSpecs.requestReturnsOk()
        )
                .perform(appointmentUUID, request);
    }

    public static CreateAppointmentResponse cancelAppointment(String appointmentUUID) {
        return cancelAppointment(appointmentUUID, AppointmentTestData.cancelStatusChangeRequest());
    }

    public static CreateAppointmentResponse updateAppointment(
            CreateAppointmentRequest request) {

        // The appointments module updates via POST /appointment with the uuid INSIDE the body -
        // the same request as create, so CrudEndpoint.update(uuid, ...) (POST /appointment/{uuid}) does not fit.
        return new SuccessfulCrudRequester<CreateAppointmentResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.APPOINTMENT_POST,
                ResponseSpecs.requestReturnsOk()
        )
                .create(request);
    }

    // POST /appointments/search answers a bare JSON array (no {"results": [...]} wrapper);
    // SuccessfulSearchRequester recognises that shape by itself
    public static List<CreateAppointmentResponse> searchAppointments(String patientUUID) {
        return new SuccessfulSearchRequester<CreateAppointmentResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.APPOINTMENTS_SEARCH,
                ResponseSpecs.requestReturnsOk()
        )
                .searchByBody(AppointmentTestData.searchRequest(patientUUID))
                .results();
    }

    // Provider linked to admin user (GET /session -> currentProvider), used as order.orderer
    public static String getCurrentProviderUuid() {
        String cached = currentProviderUuid;
        if (cached != null) {
            return cached;
        }
        synchronized (AdminSteps.class) {
            if (currentProviderUuid == null) {
                LoginAdminResponse session = new SuccessfulAuthRequester<LoginAdminResponse>(
                        RequestSpecs.unAuthSpec(),
                        Endpoint.LOGIN_GET,
                        ResponseSpecs.requestReturnsOk())
                        .login(adminCredentials());

                currentProviderUuid = session.getCurrentProvider().getUuid();
            }
            return currentProviderUuid;
        }
    }

    // Test orders (testorder) for a patient, as returned by GET /order?patient={uuid}&t=testorder&v=full
    public static SearchResult<DrugOrderResponse> fetchTestOrders(String patientUUID) {
        return new SuccessfulSearchRequester<DrugOrderResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.ORDER_GET,
                ResponseSpecs.requestReturnsOk())
                .search(OrderSearchParams.builder()
                        .patient(patientUUID)
                        .type("testorder")
                        .representation("full")
                        .build());
    }

    // Drug orders (drugorder) for a patient, as returned by GET /order?careSetting={uuid}&orderTypes={uuid}&v=full.
    // Unlike t=drugorder (which only ever returns currently active orders), careSetting+orderTypes also returns
    // stopped orders - required to see Past Medications. excludeDiscontinueOrders=true still hides the DISCONTINUE
    // stub order created by discontinueDrugOrderEncounter, leaving only the original (now stopped) order
    //
    // Params are passed as-is (SearchParams is a functional interface) rather than through
    // OrderSearchParams on purpose: this query sends "careSetting", while OrderSearchParams
    // maps its careSetting field to "caresetting". Which spelling the server honours needs
    // checking before the two are merged - see the note in the refactoring PR.
    public static SearchResult<DrugOrderResponse> fetchMedications(String patientUUID) {
        SearchParams medications = () -> Map.<String, Object>of(
                "patient", patientUUID,
                "careSetting", CareSetting.OUTPATIENT.getUuid(),
                "orderTypes", DrugOrder.ORDER_TYPE_UUID,
                "v", "full",
                "excludeDiscontinueOrders", "true");

        return new SuccessfulSearchRequester<DrugOrderResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.ORDER_GET,
                ResponseSpecs.requestReturnsOk())
                .search(medications);
    }

    // Single order by uuid, as returned by GET /order/{uuid}?v=full. Unlike fetchTestOrders,
    // this also finds orders once they are stopped/discontinued, which the list endpoint excludes
    public static Order fetchOrder(String orderUUID) {
        return new SuccessfulCrudRequester<Order>(
                RequestSpecs.adminSpec(),
                Endpoint.ORDER_POST,
                ResponseSpecs.requestReturnsOk())
                .get(orderUUID, Map.of("v", "full"));
    }

    // Single obs by uuid for a patient, as returned by GET /obs?patient={uuid}&v=full
    public static ObsResponse fetchObs(String patientUUID, String obsUUID) {
        return new SuccessfulSearchRequester<ObsResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.OBS_GET,
                ResponseSpecs.requestReturnsOk())
                .search(ObsSearchParams.builder()
                        .patient(patientUUID)
                        .representation("full")
                        .build())
                .requireOne(obs -> obs.getUuid().equals(obsUUID), "obs " + obsUUID);
    }

    // Request behind discontinueOrder, exposed so callers can send it themselves
    // (e.g. to exercise auth/error paths with a custom ResponseSpecification)
    public static DiscontinueOrderRequest discontinueOrderRequest(String orderUUID, String patientUUID, String encounterUUID) {
        return OrderTestData.discontinueOrderRequest(orderUUID, patientUUID, encounterUUID, getCurrentProviderUuid());
    }

    // Discontinues a testorder (POST /order, action=DISCONTINUE), the way the laboratory
    // stops a test order once its result has been captured
    public static Order discontinueOrder(String orderUUID, String patientUUID, String encounterUUID) {
        return new SuccessfulCrudRequester<Order>(
                RequestSpecs.adminSpec(),
                Endpoint.ORDER_POST,
                ResponseSpecs.requestReturnsCreated())
                .create(discontinueOrderRequest(orderUUID, patientUUID, encounterUUID));
    }

    // Request behind discontinueDrugOrderEncounter, exposed so callers can build the expected
    // response model from it (see apiParts.assertions.OrderAssertions)
    public static CreateEncounterRequest discontinueDrugOrderEncounterRequest(String patientUUID, String orderUUID, Drug drug) {
        return OrderTestData.discontinueDrugOrderEncounterRequest(patientUUID, orderUUID, drug, getCurrentProviderUuid());
    }

    // Discontinues a drug order (POST /encounter, action=DISCONTINUE), the way the Medications page
    // stops an active/upcoming medication; the original order ends up with dateStopped set (Past Medications)
    public static CreateEncounterResponse discontinueDrugOrderEncounter(String patientUUID, String orderUUID, Drug drug) {
        return new SuccessfulCrudRequester<CreateEncounterResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.ENCOUNTER_POST,
                ResponseSpecs.requestReturnsCreated())
                .create(discontinueDrugOrderEncounterRequest(patientUUID, orderUUID, drug));
    }

    // Updates the fulfiller status of a testorder (POST /order/{uuid}/fulfillerdetails/), the way
    // the laboratory reports progress on a test order (e.g. IN_PROGRESS, then COMPLETED)
    public static void markOrderFulfillerStatus(String orderUUID, FulfillerStatus status, String comment) {
        new ActionRequester(
                RequestSpecs.adminSpec(),
                Endpoint.ORDER_FULFILLER_DETAILS,
                ResponseSpecs.requestReturnsCreated())
                .perform(orderUUID, OrderTestData.fulfillerDetailsRequest(status, comment));
    }

    // ======== HELPERS ========
    public static String getPatientIdentifier() {
        return getId();
    }

    // Credentials of the admin user, same source as RequestSpecs.adminSpec()
    private static LoginAdminRequest adminCredentials() {
        return LoginAdminRequest.builder()
                .username(RequestSpecs.ADMIN_USERNAME)
                .password(RequestSpecs.ADMIN_PASSWORD)
                .build();
    }

    private static String getId() {
        var response = new SuccessfulCrudRequester<GetIdentifierResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.IDENTIFIER_GET,
                ResponseSpecs.requestReturnsCreated())
                .create();

        return response.getIdentifier();
    }
}
