package apiParts.steps;

import apiParts.generators.RandomModelGenerator;
import apiParts.models.*;
import apiParts.models.auth.LoginAdminRequest;
import apiParts.models.auth.LoginAdminResponse;
import apiParts.models.encounter.*;
import apiParts.models.vitals.CreateVitalsRequest;
import apiParts.models.vitals.Obs;
import apiParts.models.order.CareSetting;
import apiParts.models.order.DiscontinueOrderRequest;
import apiParts.models.order.Drug;
import apiParts.models.order.DrugOrder;
import apiParts.models.order.FulfillerDetailsRequest;
import apiParts.models.order.FulfillerStatus;
import apiParts.models.order.LabTestConcept;
import apiParts.models.order.Order;
import apiParts.models.patient.*;
import apiParts.skelethon.endpoints.Endpoint;
import apiParts.skelethon.requests.auth.SuccessfulAuthRequester;
import apiParts.skelethon.requests.crud.SuccessfulCrudRequester;
import apiParts.skelethon.requests.action.ActionRequester;
import apiParts.models.order.OrderSearchParams;
import apiParts.models.order.DrugOrderResponse;
import apiParts.models.search.SearchResult;
import apiParts.models.search.SearchParams;
import apiParts.skelethon.requests.search.SuccessfulSearchRequester;
import apiParts.specs.RequestSpecs;
import apiParts.specs.ResponseSpecs;
import apiParts.testdata.OrderTestData;
import apiParts.testdata.PatientTestData;

import java.util.List;
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

    // Request behind createDrugOrderEncounter, exposed so callers can build the expected
    // response model from it (see apiParts.assertions.OrderAssertions)
    public static CreateEncounterRequest drugOrderEncounterRequest(String patientUUID) {
        return OrderTestData.drugOrderEncounterRequest(patientUUID, getCurrentProviderUuid());
    }

    // Request behind createLabOrderEncounter, exposed for tests that need the exact
    // request instance for model comparison or an alternate request specification.
    public static CreateEncounterRequest labOrderEncounterRequest(String patientUUID) {
        return OrderTestData.labOrderEncounterRequest(patientUUID, getCurrentProviderUuid());
    }

    // Lab order encounter with the concept field intentionally omitted (concept is required
    // for a test order) - used to verify the server rejects the submission
    public static CreateEncounterRequest labOrderRequestWithoutConcept(String patientUUID) {
        return OrderTestData.labOrderRequestWithoutConcept(patientUUID, getCurrentProviderUuid());
    }

    // Lab order encounter with careSetting intentionally omitted (careSetting is required
    // for any order) - used to verify the server rejects the submission
    public static CreateEncounterRequest labOrderRequestWithoutCareSetting(String patientUUID) {
        return OrderTestData.labOrderRequestWithoutCareSetting(patientUUID, getCurrentProviderUuid());
    }

    public static CreateEncounterRequest labResultEncounterRequest(String orderUUID, Number resultValue) {
        return CreateEncounterRequest.builder()
                .obs(List.of(Obs.ofLabResult(
                        LabTestConcept.ALKALINE_PHOSPHATASE, orderUUID, resultValue)))
                .build();
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
/*
    // Single order by uuid, as returned by GET /order/{uuid}?v=full. Unlike fetchTestOrders,
    // this also finds orders once they are stopped/discontinued, which the list endpoint excludes
    public static Order fetchOrder(String orderUUID) {
        return new SuccessfulCrudRequester<Order>(
                RequestSpecs.adminSpec(),
                Endpoint.ORDER_POST,
                ResponseSpecs.requestReturnsOk())
                .get(orderUUID, new GetParams(GetParams.FULL).toQueryParams());
    }
 */

    // Single obs by uuid for a patient, as returned by GET /obs?patient={uuid}&v=full
    public static ObsResponse fetchObs(String patientUUID, String obsUUID) {
        return new SuccessfulSearchRequester<ObsResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.OBS_GET,
                ResponseSpecs.requestReturnsOk())
                .search(ObsSearchParams.builder()
                        .patient(patientUUID)
                        .representation(GetParams.FULL)
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
    public static EncounterResponse discontinueDrugOrderEncounter(String patientUUID, String orderUUID, Drug drug) {
        return new SuccessfulCrudRequester<EncounterResponse>(
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

    public static FulfillerDetailsRequest fulfillerDetailsRequest(FulfillerStatus status, String comment) {
        return OrderTestData.fulfillerDetailsRequest(status, comment);
    }

    // ======== HELPERS ========
    public static String getPatientIdentifier() {
        return getId();
    }

    // Credentials of the admin user, same source as RequestSpecs.adminSpec()
    private static LoginAdminRequest adminCredentials() {
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
