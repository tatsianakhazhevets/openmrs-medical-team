package apiParts.steps;

import apiParts.models.*;
import apiParts.models.auth.LoginAdminRequest;
import apiParts.models.auth.LoginAdminResponse;
import apiParts.models.encounter.CreateEncounterRequest;
import apiParts.models.encounter.CreateEncounterResponse;
import apiParts.models.order.GetOrderResponse;
import apiParts.models.encounter.Ref;
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
import apiParts.specs.RequestSpecs;
import apiParts.testdata.OrderTestData;
import apiParts.testdata.PatientTestData;
import apiParts.testdata.ProcedureTestData;
import apiParts.testdata.VitalsTestData;
import apiParts.specs.ResponseSpecs;

import java.time.ZoneOffset;
import java.time.Instant;
import java.util.Map;

import static apiParts.utils.DateTimeUtils.OPENMRS_RESPONSE_DATE_TIME;
import static apiParts.utils.DateTimeUtils.UTC_DATE_TIME;

public class AdminSteps {

    private static volatile String currentProviderUuid;

    // adminSpec() is authenticated by itself (Basic auth header), no login call is needed
    public static CreatePatientResponse createPatient() {
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
        var request = CreateVisitRequest.builder().patient(patientUUID)
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

        var request = CreateQueueEntryRequest.builder()
                .visit(Ref.of(visitUUID))
                .queueEntry(CreateQueueEntryRequest.QueueEntry.builder()
                        .status(QueueStatus.WAITING.toRef())
                        .priority(QueuePriority.NOT_URGENT.toRef())
                        .queue(Ref.of(queue.getUuid()))
                        .patient(Ref.of(patientUUID))
                        .startedAt(OPENMRS_RESPONSE_DATE_TIME.withZone(ZoneOffset.UTC).format(Instant.now()))
                        .sortWeight(0)
                        .build())
                .build();

        return new SuccessfulCrudRequester<QueueEntryResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.VISIT_QUEUE_ENTRY_POST,
                ResponseSpecs.requestReturnsCreated()
        ).create(request);
    }

    public static QueueEntryResponse updateQueueEntry(
            String queueEntryUUID,
            QueueStatus status,
            QueuePriority priority,
            String priorityComment) {

        var request = UpdateQueueEntryRequest.builder()
                .status(status.toRef())
                .priority(priority.toRef())
                .priorityComment(priorityComment)
                .build();

        return new SuccessfulCrudRequester<QueueEntryResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.QUEUE_ENTRY_UPDATE,
                ResponseSpecs.requestReturnsOk()
        ).update(queueEntryUUID, request);
    }

    public static QueueEntryResponse endQueueEntry(String queueEntryUUID) {
        var endRequest = EndQueueEntryRequest.builder()
                .endedAt(UTC_DATE_TIME.withZone(ZoneOffset.UTC).format(Instant.now()))
                .build();

        return new SuccessfulCrudRequester<QueueEntryResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.QUEUE_ENTRY_UPDATE,
                ResponseSpecs.requestReturnsOk()
        ).update(queueEntryUUID, endRequest);
    }

    public static GetQueueEntryResponse getActiveQueueEntries() {
        return new SuccessfulCrudRequester<GetQueueEntryResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.QUEUE_ENTRY_GET,
                ResponseSpecs.requestReturnsOk()
        ).get(Map.of(
                "v",
                "custom:(uuid,queue:(uuid,display),status:(uuid,display),patient:(uuid,display),visit:(uuid,display),priority:(uuid,display),sortWeight,startedAt,endedAt)",
                "location",
                Location.OUTPATIENT_CLINIC.getUuid(),
                "isEnded",
                "false"
        ));
    }

    public static QueueResponse getOutpatientConsultationQueue() {
        GetQueueResponse response = new SuccessfulCrudRequester<GetQueueResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.QUEUE_GET,
                ResponseSpecs.requestReturnsOk()
        ).get(Map.of(
                "v",
                "custom:(uuid,display,name,description,service:(uuid,display),allowedPriorities:(uuid,display),allowedStatuses:(uuid,display),location:(uuid,display))"
        ));

        return response.getResults().stream()
                .filter(queue ->
                        QueueType.OUTPATIENT_CONSULTATION.getDisplay().equals(queue.getName())
                                && Location.OUTPATIENT_CLINIC.getDisplay().equals(queue.getLocation().getDisplay())
                )
                .findFirst()
                .orElseThrow(() -> new AssertionError(
                        "Queue 'Outpatient Consultation' at 'Outpatient Clinic' was not found"
                ));
    }

    // Provider linked to admin user (GET /session -> currentProvider), used as order.orderer.
    // The same for the whole run: fetched once and shared by all tests, including parallel ones
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
    public static GetOrderResponse fetchTestOrders(String patientUUID) {
        return new SuccessfulCrudRequester<GetOrderResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.ORDER_GET,
                ResponseSpecs.requestReturnsOk())
                .get(Map.of("patient", patientUUID, "t", "testorder", "v", "full"));
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
