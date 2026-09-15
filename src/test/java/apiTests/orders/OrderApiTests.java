package apiTests.orders;

import apiParts.models.order.CareSetting;
import apiParts.models.order.ListOrdersResponse;
import apiParts.models.order.OrderSearchParams;
import apiParts.skelethon.endpoints.Endpoint;
import apiParts.skelethon.requests.common.SuccessfulCrudRequester;
import apiParts.specs.RequestSpecs;
import apiParts.specs.ResponseSpecs;
import apiParts.steps.AdminSteps;
import apiTests.BaseTest;
import org.junit.jupiter.api.Test;

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

        assertThat(response).as("Order search response should be deserialized")
                .isNotNull();
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

        ListOrdersResponse response = new SuccessfulCrudRequester<ListOrdersResponse>(
                RequestSpecs.unAuthSpec(),
                Endpoint.LIST_ORDERS_GET,
                ResponseSpecs.requestReturnsUnauthorized())
                .get(searchParams.toQueryParams());
    }

    @Test
    public void adminCanCheckSpecificOrderDetails(){
        
    }

}