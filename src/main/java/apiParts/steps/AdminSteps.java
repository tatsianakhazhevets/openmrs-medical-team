package apiParts.steps;

import apiParts.generators.RandomModelGenerator;
import apiParts.models.*;
import apiParts.models.auth.LoginAdminRequest;
import apiParts.models.auth.LoginAdminResponse;
import apiParts.models.encounter.*;
import apiParts.models.vitals.CreateVitalsRequest;
import apiParts.models.order.CareSetting;
import apiParts.models.order.DiscontinueOrderRequest;
import apiParts.models.order.DrugOrder;
import apiParts.models.order.FulfillerDetailsRequest;
import apiParts.models.order.FulfillerStatus;
import apiParts.models.order.Order;
import apiParts.models.patient.*;
import apiParts.skelethon.endpoints.Endpoint;
import apiParts.skelethon.requests.auth.SuccessfulAuthRequester;
import apiParts.skelethon.requests.crud.SuccessfulCrudRequester;
import apiParts.models.order.OrderSearchParams;
import apiParts.models.order.DrugOrderResponse;
import apiParts.models.search.SearchResult;
import apiParts.models.search.SearchParams;
import apiParts.skelethon.requests.search.SuccessfulSearchRequester;
import apiParts.specs.RequestSpecs;
import apiParts.specs.ResponseSpecs;
import apiParts.testdata.OrderTestData;
import apiParts.testdata.PatientTestData;

import java.util.Map;

public class AdminSteps {

    private static volatile String currentProviderUuid;

    // adminSpec() is authenticated by itself (Basic auth header), no login call is needed
    public static CreatePatientResponse createPatient() {
        LoginAdminRequest loginAdminRequest =
                RandomModelGenerator.generate(LoginAdminRequest.class);

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

    public static CreatePatientResponse createPatientWithoutInvokedIdentifier() {
        return new SuccessfulCrudRequester<CreatePatientResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.PATIENT_POST,
                ResponseSpecs.requestReturnsCreated())
                .create(PatientTestData.createPatientRequest(getId()));
    }

    // Random vitals encounter (rules in CreateVitalsRequest): one obs per VitalsConcept
    public static EncounterResponse createVitalsEncounter(String patientUUID) {
        return createEncounter(RandomModelGenerator.generate(
                CreateVitalsRequest.class, Map.of(CreateVitalsRequest.Fields.patient, patientUUID)));
    }

    public static EncounterResponse createEncounter(CreateEncounterRequest request) {
        return new SuccessfulCrudRequester<EncounterResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.ENCOUNTER_POST,
                ResponseSpecs.requestReturnsCreated()
        ).create(request);
    }

    public static EncounterResponse createEncounter(CreateVitalsRequest request) {
        return new SuccessfulCrudRequester<EncounterResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.ENCOUNTER_POST,
                ResponseSpecs.requestReturnsCreated()
        ).create(request);
    }

    // Valid outpatient drug order (Aspirin, simple dosing) as a standard fixture for order-related tests.
    // Request: OrderTestData.drugOrderEncounterRequest(patientUUID, ordererUUID)
    public static EncounterResponse createDrugOrderEncounter(String patientUUID) {
        return new SuccessfulCrudRequester<EncounterResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.ENCOUNTER_POST,
                ResponseSpecs.requestReturnsCreated())
                .create(OrderTestData.drugOrderEncounterRequest(patientUUID, getCurrentProviderUuid()));
    }

    // Valid inpatient lab order (Alkaline phosphatase test) as a standard fixture for order-related tests.
    // Request: OrderTestData.labOrderEncounterRequest(patientUUID, ordererUUID)
    public static EncounterResponse createLabOrderEncounter(String patientUUID) {
        return new SuccessfulCrudRequester<EncounterResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.ENCOUNTER_POST,
                ResponseSpecs.requestReturnsCreated())
                .create(OrderTestData.labOrderEncounterRequest(patientUUID, getCurrentProviderUuid()));
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

    public static EncounterResponse getEncounter(String encounterUUID) {
        return new SuccessfulCrudRequester<EncounterResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.ENCOUNTER_GET,
                ResponseSpecs.requestReturnsOk()
        ).get(encounterUUID, new GetParams(GetParams.FULL).toQueryParams());
    }

    // Search params for GET /order limited to a patient's inpatient orders. Exposed as params only
    // (not a request) since callers use it against RequestSpecs.unAuthSpec() to verify auth is required.
    public static OrderSearchParams inpatientOrderSearchParams(String patientUUID) {
        return OrderSearchParams.builder()
                .patient(patientUUID)
                .careSetting(CareSetting.INPATIENT.name())
                .limit(1)
                .representation("default")
                .build();
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
                        .representation(GetParams.FULL)
                        .build());
    }

    public static SearchResult<DrugOrderResponse> fetchDrugOrders(String patientUUID) {
        return new SuccessfulSearchRequester<DrugOrderResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.ORDER_GET,
                ResponseSpecs.requestReturnsOk())
                .search(OrderSearchParams.builder()
                        .patient(patientUUID)
                        .type("drugorder")
                        .representation(GetParams.FULL)
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
                "v", GetParams.FULL,
                "excludeDiscontinueOrders", "true");

        return new SuccessfulSearchRequester<DrugOrderResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.ORDER_GET,
                ResponseSpecs.requestReturnsOk())
                .search(medications);
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

    public static FulfillerDetailsRequest fulfillerDetailsRequest(FulfillerStatus status, String comment) {
        return OrderTestData.fulfillerDetailsRequest(status, comment);
    }

    // ======== HELPERS ========

    // Credentials of the admin user, same source as RequestSpecs.adminSpec()
    public static LoginAdminRequest adminCredentials() {
        return RandomModelGenerator.generate(LoginAdminRequest.class);
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
