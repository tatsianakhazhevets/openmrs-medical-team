package apiTests;

import apiParts.models.order.ListOrdersResponse;
import apiParts.models.order.OrderSearchParams;
import apiParts.skelethon.endpoints.order.OrderSearchEndpoint;
import apiParts.skelethon.endpoints.order.OrderSearchRequester;
import apiParts.skelethon.endpoints.order.SuccessfulOrderSearchRequester;
import apiParts.specs.RequestSpecs;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class OrderApiTests extends BaseTest {

    @Test
    public void adminCanFetchListOfOrders() {
        OrderSearchParams searchParams = OrderSearchParams.builder()
                .patient("fed1736c-3723-473b-b4b5-d70ddcad587f")
                .careSetting("6f0c9a92-6f24-11e3-af88-005056821db0")
                .limit(1)
                .representation("default")
                .build();

        OrderSearchEndpoint orderSearchRequester = new OrderSearchRequester(RequestSpecs.adminSpec());
        SuccessfulOrderSearchRequester successfulOrderSearchRequester = new SuccessfulOrderSearchRequester(orderSearchRequester);
        ListOrdersResponse response = successfulOrderSearchRequester.search(searchParams);
        assertThat(response).as("Order search response should be deserialized")
                .isNotNull();
    }
}