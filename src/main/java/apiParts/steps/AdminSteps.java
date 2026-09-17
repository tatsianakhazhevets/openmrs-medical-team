package apiParts.steps;

import apiParts.models.*;
import apiParts.models.auth.LoginAdminRequest;
import apiParts.models.auth.LoginAdminResponse;
import apiParts.models.encounter.CreateEncounterRequest;
import apiParts.models.encounter.CreateEncounterRequest.Obs;
import apiParts.models.encounter.CreateEncounterResponse;
import apiParts.models.order.CareSetting;
import apiParts.models.order.DosingUnit;
import apiParts.models.order.Drug;
import apiParts.models.order.DrugOrder;
import apiParts.models.order.DrugRoute;
import apiParts.models.order.GetOrderResponse;
import apiParts.models.order.LabTestConcept;
import apiParts.models.order.OrderFrequency;
import apiParts.models.order.TestOrder;
import apiParts.models.encounter.Ref;
import apiParts.models.patient.*;
import apiParts.models.procedure.BodySite;
import apiParts.models.procedure.CreateProcedureRequest;
import apiParts.models.procedure.ProcedureConcept;
import apiParts.models.procedure.ProcedureResponse;
import apiParts.models.procedure.ProcedureStatus;
import apiParts.models.procedure.ProcedureType;
import apiParts.models.queue.GetQueueResponse;
import apiParts.models.queue.QueuePriority;
import apiParts.models.queue.QueueStatus;
import apiParts.models.queueEntry.*;
import apiParts.models.visit.CreateVisitRequest;
import apiParts.models.visit.CreateVisitResponse;
import apiParts.models.visit.VisitType;
import apiParts.skelethon.endpoints.Endpoint;
import apiParts.skelethon.requests.auth.SuccessfulAuthRequester;
import apiParts.skelethon.requests.common.SuccessfulCrudRequester;
import apiParts.specs.RequestSpecs;
import apiParts.specs.ResponseSpecs;
import net.datafaker.Faker;
import org.apache.commons.lang3.StringUtils;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static apiParts.models.VitalsConcept.*;
import static apiParts.utils.DateTimeUtils.OPENMRS_REQUEST_DATE_TIME;

public class AdminSteps {

    static Faker faker = new Faker(new Locale("en", "US"));

    // startDateTime of procedureRequest(): yesterday, truncated to minutes (server does not store milliseconds).
    // Fixed for the whole run, so procedureRequest() is the same request as sent by createProcedure()
    // and tests can build dates relative to the created procedure
    public static final OffsetDateTime PROCEDURE_START = OffsetDateTime.now(ZoneOffset.ofHours(3)).minusDays(1).truncatedTo(ChronoUnit.MINUTES);

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

        String gender = faker.gender().binaryTypes(); // "Male" / "Female"
        String shortGender = gender.equals("Male") ? "M" : "F";

        CreatePatientRequest createPatientRequest = CreatePatientRequest.builder()
                .person(PersonRequest.builder()
                        .gender(shortGender)
                        .birthdate(faker.timeAndDate().birthday(18, 65, "yyyy-MM-dd"))
                        .birthdateEstimated(false)
                        .dead(false)
                        .names(List.of(
                                PersonName.builder()
                                        .givenName(faker.name().firstName())
                                        .familyName(faker.name().lastName())
                                        .build()))
                        .addresses(List.of(
                                PersonAddress.builder()
                                        .address1(faker.address().streetAddress())
                                        .cityVillage(faker.address().city())
                                        .country(StringUtils.left(faker.address().country(), 50))  //was flaky because of "country": "British Indian Ocean Territory (Chagos Archipelago)"
                                        .postalCode(faker.address().postcode())
                                        .build()))
                        .build())
                .identifiers(List.of(
                        PatientIdentifierRequest.builder()
                                .identifier(getId())
                                .identifierType("05a29f94-c0ed-11e2-94be-8c13b969e334") //MRS ID GET /openmrs/ws/rest/v1/patientidentifiertype?v=custom:(uuid,name,required,uniquenessBehavior,locationBehavior)
                                .location("dbdaabf6-a326-4804-aba7-062073e05cd1") //Outpatient Clinic DOTO - move to ENUM?
                                .preferred(true)
                                .build()))
                .build();

        return new SuccessfulCrudRequester<CreatePatientResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.PATIENT_POST,
                ResponseSpecs.requestReturnsCreated())
                .create(createPatientRequest);
    }

    public static CreateEncounterResponse createVitalsEncounter(String patientUUID) {
        CreateEncounterRequest createEncounterRequest = CreateEncounterRequest.builder()
                .patient(patientUUID)
                .encounterType(EncounterType.VITALS)
                .location(Location.OUTPATIENT_CLINIC)
                .obs(List.of(
                        Obs.of(SYSTOLIC_BP, 100),
                        Obs.of(DIASTOLIC_BP, 70),
                        Obs.of(RESPIRATORY_RATE, 14),
                        Obs.of(OXYGEN_SATURATION, 95),
                        Obs.of(PULSE, 68),
                        Obs.of(TEMPERATURE, 37),
                        Obs.of(GENERAL_NOTE, "Some note"),
                        Obs.of(WEIGHT, 90.2),
                        Obs.of(HEIGHT, 177.3),
                        Obs.of(MID_UPPER_ARM_CIRC, 14),
                        Obs.of(BMI, 28.7)))
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

    // Valid outpatient drug order (Aspirin, simple dosing) as a standard fixture for order-related tests
    public static CreateEncounterResponse createDrugOrderEncounter(String patientUUID) {
        return new SuccessfulCrudRequester<CreateEncounterResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.ENCOUNTER_POST,
                ResponseSpecs.requestReturnsCreated())
                .create(drugOrderEncounterRequest(patientUUID));
    }

    // Request behind createDrugOrderEncounter, exposed so callers can build the expected
    // response model from it (see apiParts.assertions.OrderAssertions)
    public static CreateEncounterRequest drugOrderEncounterRequest(String patientUUID) {
        DrugOrder order = DrugOrder.builder()
                .patient(patientUUID)
                .careSetting(CareSetting.OUTPATIENT)
                .orderer(getCurrentProviderUuid())
                .drug(Drug.ASPIRIN_325MG)
                .dose(1.0)
                .doseUnits(DosingUnit.TABLET)
                .route(DrugRoute.ORAL)
                .frequency(OrderFrequency.ONCE_DAILY)
                .quantity(5.0)
                .quantityUnits(DosingUnit.TABLET)
                .numRefills(1)
                .build();

        return CreateEncounterRequest.builder()
                .patient(patientUUID)
                .encounterType(EncounterType.ORDER)
                .location(Location.OUTPATIENT_CLINIC)
                .orders(List.of(order))
                .build();
    }

    // Valid inpatient lab order (Alkaline phosphatase test) as a standard fixture for order-related tests
    public static CreateEncounterResponse createLabOrderEncounter(String patientUUID) {
        return new SuccessfulCrudRequester<CreateEncounterResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.ENCOUNTER_POST,
                ResponseSpecs.requestReturnsCreated())
                .create(labOrderEncounterRequest(patientUUID));
    }

    // Request behind createLabOrderEncounter, exposed so callers can build the expected
    // response model from it (see apiParts.assertions.ModelAssertions)
    public static CreateEncounterRequest labOrderEncounterRequest(String patientUUID) {
        TestOrder order = TestOrder.builder()
                .patient(patientUUID)
                .careSetting(CareSetting.OUTPATIENT)
                .orderer(getCurrentProviderUuid())
                .concept(LabTestConcept.ALKALINE_PHOSPHATASE)
                .instructions("test")
                .accessionNumber("1")
                .build();

        return CreateEncounterRequest.builder()
                .patient(patientUUID)
                .encounterType(EncounterType.ORDER)
                .location(Location.INPATIENT_WARD)
                .orders(List.of(order))
                .build();
    }

    // Valid procedure with required fields only (Laparoscopic cholecystectomy, started yesterday)
    // as a standard fixture for procedure tests
    public static ProcedureResponse createProcedure(String patientUUID) {
        return new SuccessfulCrudRequester<ProcedureResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.PROCEDURE_POST,
                ResponseSpecs.requestReturnsCreated())
                .create(procedureRequest(patientUUID));
    }

    // Request behind createProcedure, exposed so callers can build the expected
    // response model from it (see apiParts.assertions.ProcedureAssertions)
    public static CreateProcedureRequest procedureRequest(String patientUUID) {
        return CreateProcedureRequest.builder()
                .patient(patientUUID)
                .procedureCoded(ProcedureConcept.LAPAROSCOPIC_CHOLECYSTECTOMY.getUuid())
                .procedureType(ProcedureType.EMERGENCY.getUuid())
                .bodySite(BodySite.ABDOMEN.getUuid())
                .startDateTime(PROCEDURE_START.format(OPENMRS_REQUEST_DATE_TIME))
                .status(ProcedureStatus.COMPLETED.getUuid())
                .build();
    }

    public static void deleteVisit(String visitUUID) {
        new SuccessfulCrudRequester<CreateVisitResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.VISIT_DELETE,
                ResponseSpecs.requestReturnsNoContent()
        ).delete(visitUUID, Map.of("purge", true));
    }

    public static CreateQueueEntryResponse addPatientToQueue(String patientUUID, String visitUUID) {
        GetQueueResponse getQueueResponse = new SuccessfulCrudRequester<GetQueueResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.QUEUE_GET,
                ResponseSpecs.requestReturnsOk()
        ).get(Map.of(
                "v",
                "custom:(uuid,display,name,description,service:(uuid,display),allowedPriorities:(uuid,display),allowedStatuses:(uuid,display),location:(uuid,display))"
        ));
        var queue = getQueueResponse.getResults().stream()
                .filter(q ->
                        "Outpatient Consultation".equals(q.getName())
                                && "Outpatient Clinic".equals(q.getLocation().getDisplay())
                )
                .findFirst()
                .orElseThrow(() -> new AssertionError(
                        "Queue 'Outpatient Consultation' at 'Outpatient Clinic' was not found"
                ));

        var request = CreateQueueEntryRequest.builder()
                .visit(Ref.of(visitUUID))
                .queueEntry(CreateQueueEntryRequest.QueueEntry.builder()
                        .status(QueueStatus.WAITING.toRef())
                        .priority(QueuePriority.NOT_URGENT.toRef())
                        .queue(Ref.of(queue.getUuid()))
                        .patient(Ref.of(patientUUID))
                        .startedAt(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
                                .withZone(ZoneOffset.UTC)
                                .format(Instant.now()))
                        .sortWeight(0)
                        .build())
                .build();

        return new SuccessfulCrudRequester<CreateQueueEntryResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.VISIT_QUEUE_ENTRY_POST,
                ResponseSpecs.requestReturnsCreated()
        ).create(request);
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
                "44c3efb0-2583-4c80-a79e-1f756a03c0a1",
                "isEnded",
                "false"
        ));
    }

    public static CreateQueueEntryResponse updateQueueEntry(
            String queueEntryUUID,
            QueueStatus status,
            QueuePriority priority,
            String priorityComment) {

        var request = UpdateQueueEntryRequest.builder()
                .status(status.toRef())
                .priority(priority.toRef())
                .priorityComment(priorityComment)
                .build();

        return new SuccessfulCrudRequester<CreateQueueEntryResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.QUEUE_ENTRY_UPDATE,
                ResponseSpecs.requestReturnsOk()
        ).update(queueEntryUUID, request);
    }

    public static CreateQueueEntryResponse endQueueEntry(String queueEntryUUID) {
        var endRequest = EndQueueEntryRequest.builder()
                .endedAt(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                        .withZone(ZoneOffset.UTC)
                        .format(Instant.now()))
                .build();

        return new SuccessfulCrudRequester<CreateQueueEntryResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.QUEUE_ENTRY_UPDATE,
                ResponseSpecs.requestReturnsOk()
        ).update(queueEntryUUID, endRequest);
    }

    // Provider linked to admin user (GET /session -> currentProvider), used as order.orderer
    public static String getCurrentProviderUuid() {
        LoginAdminRequest loginAdminRequest = LoginAdminRequest.builder()
                .username("admin")
                .password("Admin123")
                .build();

        LoginAdminResponse session = new SuccessfulAuthRequester<LoginAdminResponse>(
                RequestSpecs.unAuthSpec(),
                Endpoint.LOGIN_GET,
                ResponseSpecs.requestReturnsOk())
                .login(loginAdminRequest);

        return session.getCurrentProvider().getUuid();
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

    private static String getId() {
        var response = new SuccessfulCrudRequester<GetIdentifierResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.IDENTIFIER_GET,
                ResponseSpecs.requestReturnsCreated())
                .create();

        return response.getIdentifier();
    }
}
