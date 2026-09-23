package apiTests.orders;

import apiParts.assertions.ModelAssertions;
import apiParts.assertions.OrderAssertions;
import apiParts.models.EncounterType;
import apiParts.models.Location;
import apiParts.models.encounter.CreateEncounterRequest;
import apiParts.models.encounter.CreateEncounterResponse;
import apiParts.models.encounter.Ref;
import apiParts.models.order.CareSetting;
import apiParts.models.order.OrderSearchParams;
import apiParts.skelethon.endpoints.Endpoint;
import apiParts.skelethon.requests.crud.SuccessfulCrudRequester;
import apiParts.models.search.SearchResult;
import apiParts.models.order.Order;
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

        SearchResult<Order> orders = new SuccessfulSearchRequester<Order>(
                RequestSpecs.adminSpec(),
                Endpoint.LIST_ORDERS_GET,
                ResponseSpecs.requestReturnsOk())
                .search(searchParams);

        assertThat(orders.results()).isNotNull();
    }

    @Test
    public void authRequiredToFetchListOfOrders() {
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

    // Precondition: patient with a standard drug order encounter
    @Test
    @CreateOrder(DRUG)
    public void adminCanCheckSpecificOrderDetails() {
        String patientUUID = SessionStorage.getPatient().getUuid();
        CreateEncounterRequest drugOrderRequest = OrderTestData.drugOrderEncounterRequest(patientUUID, AdminSteps.getCurrentProviderUuid());
        String orderUUID = SessionStorage.getOrderUuid();

        var savedOrder = new SuccessfulSearchRequester<DrugOrderResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.ORDER_GET,
                ResponseSpecs.requestReturnsOk())
                .search(OrderSearchParams.builder()
                        .patient(patientUUID)
                        .type("drugorder")
                        .representation("full")
                        .build())
                .requireOne(order -> order.getUuid().equals(orderUUID), "created order " + orderUUID);

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
}
