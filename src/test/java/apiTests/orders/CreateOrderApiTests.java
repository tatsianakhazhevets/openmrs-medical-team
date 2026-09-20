package apiTests.orders;

import apiParts.assertions.ModelAssertions;
import apiParts.assertions.OrderAssertions;
import apiParts.models.EncounterType;
import apiParts.models.Location;
import apiParts.models.encounter.CreateEncounterRequest;
import apiParts.models.encounter.CreateEncounterResponse;
import apiParts.models.encounter.Ref;
import apiParts.models.order.CareSetting;
import apiParts.models.order.GetOrderResponse;
import apiParts.models.order.LabTestConcept;
import apiParts.models.order.TestOrder;
import apiParts.skelethon.endpoints.Endpoint;
import apiParts.skelethon.requests.common.CrudRequester;
import apiParts.skelethon.requests.common.SuccessfulCrudRequester;
import apiParts.models.order.DrugOrderResponse;
import apiParts.models.order.OrderSearchParams;
import apiParts.skelethon.requests.search.SuccessfulSearchRequester;
import apiParts.specs.RequestSpecs;
import apiParts.specs.ResponseSpecs;
import apiParts.steps.AdminSteps;
import apiParts.testdata.OrderTestData;
import apiTests.BaseTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

public class CreateOrderApiTests extends BaseTest {

    @Test
    public void adminCanCreateDrugOrder() {
        // create a patient with a standard drug order encounter
        var patientResponse = AdminSteps.createPatient();
        String patientUUID = patientResponse.getUuid();

        CreateEncounterRequest drugOrderRequest = OrderTestData.drugOrderEncounterRequest(patientUUID, AdminSteps.getCurrentProviderUuid());
        var encounter = new SuccessfulCrudRequester<CreateEncounterResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.ENCOUNTER_POST,
                ResponseSpecs.requestReturnsCreated())
                .create(drugOrderRequest);

        softly.assertThat(encounter.getOrders())
                .as("created drug order")
                .hasSize(1);
        String orderUUID = encounter.getOrders().get(0).getUuid();

        // verify the order was actually persisted and matches what was sent
        var patientOrders = new SuccessfulCrudRequester<GetOrderResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.ORDER_GET,
                ResponseSpecs.requestReturnsOk())
                .get(Map.of("patient", patientUUID, "t", "drugorder", "v", "full"));

        var savedOrder = patientOrders.getResults().stream()
                .filter(result -> result.getUuid().equals(orderUUID))
                .findFirst()
                .orElseThrow(() -> new AssertionError("created order " + orderUUID + " not found in GET /order response"));

        ModelAssertions.assertMatchesExpected(softly,
                savedOrder,
                OrderAssertions.expectedOrdersOf(drugOrderRequest).get(0),
                "saved drug order");
    }

    @Test
    public void adminCanCreateLabOrder() {
        // create a patient with a lab order (Alkaline phosphatase test) encounter
        var patientResponse = AdminSteps.createPatient();
        String patientUUID = patientResponse.getUuid();

        CreateEncounterRequest labOrderRequest = OrderTestData.labOrderEncounterRequest(patientUUID, AdminSteps.getCurrentProviderUuid());
        var labEncounter = new SuccessfulCrudRequester<CreateEncounterResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.ENCOUNTER_POST,
                ResponseSpecs.requestReturnsCreated())
                .create(labOrderRequest);

        CreateEncounterResponse expectedLabEncounter = new CreateEncounterResponse();
        expectedLabEncounter.setPatient(Ref.of(patientUUID));
        expectedLabEncounter.setLocation(Ref.of(Location.INPATIENT_WARD.getUuid()));
        expectedLabEncounter.setEncounterType(Ref.of(EncounterType.ORDER.getUuid()));

        ModelAssertions.assertMatchesExpected(softly, labEncounter, expectedLabEncounter, "lab order encounter");

        softly.assertThat(labEncounter.getObs())
                .as("lab encounter obs")
                .isEmpty();
        softly.assertThat(labEncounter.getVoided())
                .as("lab encounter voided")
                .isFalse();
        softly.assertThat(labEncounter.getOrders())
                .as("lab encounter orders")
                .hasSize(1);
        softly.assertThat(labEncounter.getOrders().get(0).getDisplay())
                .as("lab order display")
                .isEqualTo("Alkaline phosphatase");

        // verify the order was actually persisted
        String orderUUID = labEncounter.getOrders().get(0).getUuid();
        var patientOrders = new SuccessfulCrudRequester<GetOrderResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.ORDER_GET,
                ResponseSpecs.requestReturnsOk())
                .get(Map.of("patient", patientUUID, "t", "testorder", "v", "full"));

        softly.assertThat(patientOrders.getResults())
                .as("created lab order is retrievable via GET /order")
                .anyMatch(order -> order.getUuid().equals(orderUUID));
    }

    @Test
    @DisplayName("auth required to create order")
    public void authRequiredToCreateOrder() {
        // create patient
        var patientResponse = AdminSteps.createPatient();

        CreateEncounterRequest labOrderRequest = OrderTestData.labOrderEncounterRequest(patientResponse.getUuid(), AdminSteps.getCurrentProviderUuid());

        new CrudRequester(
                RequestSpecs.unAuthSpec(),
                Endpoint.ENCOUNTER_POST,
                ResponseSpecs.requestReturnsBadRequest())
                .create(labOrderRequest);
    }

    @Test
    public void adminCannotCreateOrderWithoutRequiredConcept() {
        // create patient
        var patientResponse = AdminSteps.createPatient();
        String patientUUID = patientResponse.getUuid();

        // concept is required for a test order and is intentionally omitted here
        TestOrder invalidOrder = TestOrder.builder()
                .patient(patientUUID)
                .careSetting(CareSetting.INPATIENT)
                .orderer(AdminSteps.getCurrentProviderUuid())
                .instructions("test")
                .accessionNumber("1")
                .build();

        CreateEncounterRequest request = CreateEncounterRequest.builder()
                .patient(patientUUID)
                .encounterType(EncounterType.ORDER)
                .location(Location.INPATIENT_WARD)
                .orders(List.of(invalidOrder))
                .build();

        new CrudRequester(
                RequestSpecs.adminSpec(),
                Endpoint.ENCOUNTER_POST,
                ResponseSpecs.requestReturnsInvalidSubmission("concept"))
                .create(request);
    }

    @Test
    @DisplayName("admin cannot create order without careSetting")
    public void adminCannotCreateOrderWithoutRequiredCareSetting() {
        // create patient
        var patientResponse = AdminSteps.createPatient();
        String patientUUID = patientResponse.getUuid();

        // careSetting is required for an order and is intentionally omitted here
        TestOrder invalidOrder = TestOrder.builder()
                .patient(patientUUID)
                .orderer(AdminSteps.getCurrentProviderUuid())
                .concept(LabTestConcept.ALKALINE_PHOSPHATASE)
                .instructions("test")
                .accessionNumber("1")
                .build();

        CreateEncounterRequest request = CreateEncounterRequest.builder()
                .patient(patientUUID)
                .encounterType(EncounterType.ORDER)
                .location(Location.INPATIENT_WARD)
                .orders(List.of(invalidOrder))
                .build();

        new CrudRequester(
                RequestSpecs.adminSpec(),
                Endpoint.ENCOUNTER_POST,
                ResponseSpecs.requestReturnsServerError())
                .create(request);
    }


    // ==== Search-based variant of the "order was persisted" check. Originals untouched. ====
    // GET /order?patient=...&t=drugorder answers a collection, so it is a search.
    // Map.of(...) becomes typed params and the stream lookup becomes requireOne().
    @Test
    public void createdDrugOrderIsReturnedBySearchViaSearchRequester() {
        var patientResponse = AdminSteps.createPatient();
        String patientUUID = patientResponse.getUuid();

        CreateEncounterRequest drugOrderRequest =
                OrderTestData.drugOrderEncounterRequest(patientUUID, AdminSteps.getCurrentProviderUuid());

        var encounter = new SuccessfulCrudRequester<CreateEncounterResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.ENCOUNTER_POST,
                ResponseSpecs.requestReturnsCreated())
                .create(drugOrderRequest);

        softly.assertThat(encounter.getOrders()).as("created drug order").hasSize(1);
        String orderUUID = encounter.getOrders().get(0).getUuid();

        DrugOrderResponse savedOrder = new SuccessfulSearchRequester<DrugOrderResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.ORDER_GET,
                ResponseSpecs.requestReturnsOk())
                .search(OrderSearchParams.builder()
                        .patient(patientUUID)
                        .type("drugorder")
                        .representation("full")
                        .build())
                .requireOne(order -> order.getUuid().equals(orderUUID),
                        "created order " + orderUUID);

        ModelAssertions.assertMatchesExpected(softly,
                savedOrder,
                OrderAssertions.expectedOrdersOf(drugOrderRequest).get(0),
                "drug order found by search");
    }

}
