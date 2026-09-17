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
import apiParts.specs.RequestSpecs;
import apiParts.specs.ResponseSpecs;
import apiParts.steps.AdminSteps;
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
        CreateEncounterRequest drugOrderRequest = AdminSteps.drugOrderEncounterRequest(patientUUID);
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
        CreateEncounterRequest labOrderRequest = AdminSteps.labOrderEncounterRequest(patientUUID);
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