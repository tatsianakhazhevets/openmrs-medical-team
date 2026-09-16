package apiTests.orders;

import apiParts.models.order.CareSetting;
import apiParts.models.order.Drug;
import apiParts.models.order.DosingUnit;
import apiParts.models.order.DrugRoute;
import apiParts.models.order.GetOrderResponse;
import apiParts.models.order.ListOrdersResponse;
import apiParts.models.order.OrderFrequency;
import apiParts.models.order.OrderSearchParams;
import apiParts.skelethon.endpoints.Endpoint;
import apiParts.skelethon.requests.common.SuccessfulCrudRequester;
import apiParts.specs.RequestSpecs;
import apiParts.specs.ResponseSpecs;
import apiParts.steps.AdminSteps;
import apiTests.BaseTest;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class OrderApiTests extends BaseTest {

    @Test
    public void adminCanFetchListOfOrders() {

        // create patient
        var patientResponse = AdminSteps.createPatient();

        OrderSearchParams searchParams = OrderSearchParams.builder()
                .patient(patientResponse.getUuid())
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

        // create patient
        var patientResponse = AdminSteps.createPatient();

        OrderSearchParams searchParams = OrderSearchParams.builder()
                .patient(patientResponse.getUuid())
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

    @Test
    public void adminCanCheckSpecificOrderDetails() {
        // create a patient with a standard drug order encounter
        var patientResponse = AdminSteps.createPatient();
        String patientUUID = patientResponse.getUuid();

        var encounter = AdminSteps.createDrugOrderEncounter(patientUUID);
        String orderUUID = encounter.getOrders().get(0).getUuid();

        var patientOrders = new SuccessfulCrudRequester<GetOrderResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.ORDER_GET,
                ResponseSpecs.requestReturnsOk())
                .get(Map.of("patient", patientUUID, "t", "drugorder", "v", "full"));

        var savedOrder = patientOrders.getResults().stream()
                .filter(result -> result.getUuid().equals(orderUUID))
                .findFirst()
                .orElseThrow(() -> new AssertionError("created order " + orderUUID + " not found in GET /order response"));

        softly.assertThat(savedOrder.getPatient().getUuid())
                .as("order patient uuid")
                .isEqualTo(patientUUID);
        softly.assertThat(savedOrder.getDrug().getUuid())
                .as("order drug uuid")
                .isEqualTo(Drug.ASPIRIN_325MG.getUuid());
        softly.assertThat(savedOrder.getCareSetting().getUuid())
                .as("order care setting uuid")
                .isEqualTo(CareSetting.OUTPATIENT.getUuid());
        softly.assertThat(savedOrder.getDose())
                .as("order dose")
                .isEqualTo(1.0);
        softly.assertThat(savedOrder.getDoseUnits().getUuid())
                .as("order dose units uuid")
                .isEqualTo(DosingUnit.TABLET.getUuid());
        softly.assertThat(savedOrder.getRoute().getUuid())
                .as("order route uuid")
                .isEqualTo(DrugRoute.ORAL.getUuid());
        softly.assertThat(savedOrder.getFrequency().getUuid())
                .as("order frequency uuid")
                .isEqualTo(OrderFrequency.ONCE_DAILY.getUuid());
        softly.assertThat(savedOrder.getQuantity())
                .as("order quantity")
                .isEqualTo(5.0);
        softly.assertThat(savedOrder.getNumRefills())
                .as("order num refills")
                .isEqualTo(1);
    }

}