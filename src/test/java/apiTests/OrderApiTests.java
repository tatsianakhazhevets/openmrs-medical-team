package apiTests;

import apiParts.models.order.ListOrdersResponse;
import apiParts.models.order.OrderSearchParams;
import apiParts.skelethon.endpoints.Endpoint;
import apiParts.skelethon.requests.common.SuccessfulCrudRequester;
import apiParts.specs.RequestSpecs;
import apiParts.specs.ResponseSpecs;
import apiParts.steps.AdminSteps;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class OrderApiTests extends BaseTest {

    @Test
    public void adminCanFetchListOfOrders() {
        // create patient

        var patientResponse = AdminSteps.createPatient();

        OrderSearchParams searchParams = OrderSearchParams.builder()
                .patient(patientResponse.getUuid())
                .careSetting("6f0c9a92-6f24-11e3-af88-005056821db0")
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
}