package apiParts.steps;

import apiParts.models.*;
import apiParts.models.appointment.*;
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
import apiParts.models.queue.*;
import apiParts.models.queueEntry.*;
import apiParts.models.visit.CreateVisitRequest;
import apiParts.models.visit.CreateVisitResponse;
import apiParts.models.visit.VisitType;
import apiParts.skelethon.endpoints.Endpoint;
import apiParts.skelethon.requests.appointment.AppointmentRequester;
import apiParts.skelethon.requests.auth.SuccessfulAuthRequester;
import apiParts.skelethon.requests.common.CrudRequester;
import apiParts.skelethon.requests.common.SuccessfulCrudRequester;
import apiParts.specs.RequestSpecs;
import apiParts.testdata.OrderTestData;
import apiParts.testdata.PatientTestData;
import apiParts.testdata.ProcedureTestData;
import apiParts.testdata.VitalsTestData;
import apiParts.specs.ResponseSpecs;
import apiParts.utils.DateTimeUtils;
import io.restassured.common.mapper.TypeRef;
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

import static apiParts.utils.DateTimeUtils.OPENMRS_RESPONSE_DATE_TIME;
import static apiParts.utils.DateTimeUtils.UTC_DATE_TIME;

public class AdminSteps {

    private static volatile String currentProviderUuid;
    private static final Faker FAKER = new Faker(new Locale("en", "US"));

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

        String gender = FAKER.gender().binaryTypes(); // "Male" / "Female"
        String shortGender = gender.equals("Male") ? "M" : "F";

        CreatePatientRequest createPatientRequest = CreatePatientRequest.builder()
                .person(PersonRequest.builder()
                        .gender(shortGender)
                        .birthdate(FAKER.timeAndDate().birthday(18, 65, "yyyy-MM-dd"))
                        .birthdateEstimated(false)
                        .dead(false)
                        .names(List.of(
                                PersonName.builder()
                                        .givenName(FAKER.name().firstName())
                                        .familyName(FAKER.name().lastName())
                                        .build()))
                        .addresses(List.of(
                                PersonAddress.builder()
                                        .address1(FAKER.address().streetAddress())
                                        .cityVillage(FAKER.address().city())
                                        .country(StringUtils.left(FAKER.address().country(), 50))  //was flaky because of "country": "British Indian Ocean Territory (Chagos Archipelago)"
                                        .postalCode(FAKER.address().postcode())
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

        CreateQueueEntryRequest request = CreateQueueEntryRequest.builder()
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

        UpdateQueueEntryRequest request = UpdateQueueEntryRequest.builder()
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
        EndQueueEntryRequest endRequest = EndQueueEntryRequest.builder()
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
    public static CreateAppointmentResponse createAppointment(CreateAppointmentRequest request) {
        return new SuccessfulCrudRequester<CreateAppointmentResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.APPOINTMENT_POST,
                ResponseSpecs.requestReturnsOk()
        ).create(request);
    }

    public static CreateAppointmentResponse createAppointment(String patientUUID) {
        OffsetDateTime startDateTime = DateTimeUtils.nowPlusMinutes(30);
        OffsetDateTime endDateTime = startDateTime.plusMinutes(30);

        CreateAppointmentRequest request = CreateAppointmentRequest.builder()
                .appointmentKind(AppointmentKind.SCHEDULED.getValue())
                .status("")
                .serviceUuid(AppointmentService.GENERAL_MEDICINE.getUuid())
                .startDateTime(startDateTime.format(DateTimeUtils.OPENMRS_REQUEST_DATE_TIME))
                .endDateTime(endDateTime.format(DateTimeUtils.OPENMRS_REQUEST_DATE_TIME))
                .locationUuid(Location.OUTPATIENT_CLINIC.getUuid())
                .providers(List.of(
                        CreateAppointmentRequest.Provider.builder()
                                .uuid(AppointmentProvider.SUPER_USER.getUuid())
                                .build()
                ))
                .patientUuid(patientUUID)
                .comments(FAKER.text().text())
                .dateAppointmentScheduled(
                        DateTimeUtils.now()
                                .format(DateTimeUtils.OPENMRS_REQUEST_DATE_TIME)
                )
                .build();

        return createAppointment(request);
    }

    public static CreateAppointmentResponse cancelAppointment(
            String appointmentUUID,
            AppointmentStatusChangeRequest request) {

        return new AppointmentRequester(
                RequestSpecs.adminSpec(),
                Endpoint.APPOINTMENT_STATUS_CHANGE,
                ResponseSpecs.requestReturnsOk()
        )
                .changeStatus(appointmentUUID, request)
                .extract()
                .as(CreateAppointmentResponse.class);
    }

    public static CreateAppointmentResponse cancelAppointment(String appointmentUUID) {
        AppointmentStatusChangeRequest request = AppointmentStatusChangeRequest.builder()
                .toStatus(AppointmentStatus.CANCELLED.getValue())
                .onDate(
                        OffsetDateTime.now()
                                .format(DateTimeFormatter.ofPattern(
                                        "yyyy-MM-dd'T'HH:mm:ss.SSSZ"
                                ))
                )
                .timeZone("Asia/Yerevan")
                .build();

        return cancelAppointment(appointmentUUID, request);
    }

    public static CreateAppointmentResponse updateAppointment(
            CreateAppointmentRequest request) {

        return new AppointmentRequester(
                RequestSpecs.adminSpec(),
                Endpoint.APPOINTMENT_POST,
                ResponseSpecs.requestReturnsOk()
        )
                .update(request)
                .extract()
                .as(CreateAppointmentResponse.class);
    }

    public static List<CreateAppointmentResponse> searchAppointments(String patientUUID) {
        AppointmentSearchRequest request = AppointmentSearchRequest.builder()
                .patientUuid(patientUUID)
                .startDate(
                        DateTimeUtils.nowMinusMonths(6)
                                .toInstant()
                                .toString()
                )
                .build();

        return new AppointmentRequester(
                RequestSpecs.adminSpec(),
                Endpoint.APPOINTMENTS_SEARCH,
                ResponseSpecs.requestReturnsOk()
        )
                .search(request)
                .extract()
                .jsonPath()
                .getList("", CreateAppointmentResponse.class);
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
