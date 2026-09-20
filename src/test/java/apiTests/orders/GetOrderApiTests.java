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
import apiParts.models.order.ListOrdersResponse;
import apiParts.models.order.OrderSearchParams;
import apiParts.skelethon.endpoints.Endpoint;
import apiParts.skelethon.requests.common.SuccessfulCrudRequester;
import apiParts.skelethon.requests.search.SuccessfulSearchRequester;
import apiParts.skelethon.requests.search.SearchRequester;
import apiParts.models.order.DrugOrderResponse;
import apiParts.specs.RequestSpecs;
import apiParts.specs.ResponseSpecs;
import apiParts.steps.AdminSteps;
import apiParts.testdata.OrderTestData;
import apiTests.BaseTest;
import common.annotations.CreateOrder;
import common.annotations.CreatePatient;
import common.storages.SessionStorage;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static common.annotations.CreateOrder.Type.DRUG;
import static org.assertj.core.api.Assertions.assertThat;

@CreatePatient
public class GetOrderApiTests extends BaseTest {

    @Test
    public void adminCanFetchListOfOrders() {
        OrderSearchParams searchParams = OrderSearchParams.builder()
                .patient(SessionStorage.getPatient().getUuid())
                .careSetting(CareSetting.INPATIENT.name())
                .limit(1)
                .representation("default")
                .build();

        ListOrdersResponse response = new SuccessfulCrudRequester<ListOrdersResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.LIST_ORDERS_GET,
                ResponseSpecs.requestReturnsOk())
                .get(searchParams.toQueryParams());

        assertThat(response).isNotNull();
    }

    @Test
    public void authRequiredToFetchListOfOrders() {
        OrderSearchParams searchParams = OrderSearchParams.builder()
                .patient(SessionStorage.getPatient().getUuid())
                .careSetting(CareSetting.INPATIENT.name())
                .limit(1)
                .representation("default")
                .build();

        new SuccessfulCrudRequester<ListOrdersResponse>(
                RequestSpecs.unAuthSpec(),
                Endpoint.LIST_ORDERS_GET,
                ResponseSpecs.requestReturnsUnauthorized())
                .get(searchParams.toQueryParams());
    }

    // Precondition: patient with a standard drug order encounter
    @Test
    @CreateOrder(DRUG)
    public void adminCanCheckSpecificOrderDetails() {
        String patientUUID = SessionStorage.getPatient().getUuid();
        CreateEncounterRequest drugOrderRequest = OrderTestData.drugOrderEncounterRequest(patientUUID, AdminSteps.getCurrentProviderUuid());
        String orderUUID = SessionStorage.getOrderUuid();

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

        // add a lab order (Alkaline phosphatase test) encounter for the same patient
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
    }


    // ==== COPY of authRequiredToFetchListOfOrders built on the raw SearchRequester.
    //      Original untouched. ====
    // Negative scenarios belong on the raw requester: it never tries to deserialize
    // a body that a 401 does not have.
    @Test
    public void authRequiredToFetchListOfOrdersViaSearch() {
        OrderSearchParams searchParams = OrderSearchParams.builder()
                .patient(SessionStorage.getPatient().getUuid())
                .careSetting(CareSetting.INPATIENT.name())
                .limit(1)
                .representation("default")
                .build();

        new SearchRequester(
                RequestSpecs.unAuthSpec(),
                Endpoint.LIST_ORDERS_GET,
                ResponseSpecs.requestReturnsUnauthorized())
                .search(searchParams);
    }

    // ==== Search-based variant of the lookup done inside adminCanCheckSpecificOrderDetails.
    //      Original untouched. ====
    // Replaces Map.of("patient", ..., "t", "drugorder", "v", "full") with typed params,
    // and getResults().stream().filter(...).findFirst().orElseThrow(...) with requireOne().
    @Test
    @CreateOrder(DRUG)
    public void adminCanFindCreatedOrderViaSearch() {
        String patientUUID = SessionStorage.getPatient().getUuid();
        String orderUUID = SessionStorage.getOrderUuid();

        OrderSearchParams searchParams = OrderSearchParams.builder()
                .patient(patientUUID)
                .type("drugorder")
                .representation("full")
                .build();

        DrugOrderResponse savedOrder = new SuccessfulSearchRequester<DrugOrderResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.ORDER_GET,
                ResponseSpecs.requestReturnsOk())
                .search(searchParams)
                .requireOne(order -> order.getUuid().equals(orderUUID), "created order " + orderUUID);

        softly.assertThat(savedOrder.getUuid()).isEqualTo(orderUUID);
    }

}
