package apiParts.skelethon.endpoints.order;

import apiParts.models.order.ListOrdersResponse;
import apiParts.models.order.OrderSearchParams;
import io.restassured.response.Response;
import lombok.RequiredArgsConstructor;

import static org.apache.http.HttpStatus.SC_OK;

@RequiredArgsConstructor
public class SuccessfulOrderSearchRequester {

    private final OrderSearchEndpoint requester;

    public ListOrdersResponse search(OrderSearchParams params) {
        Response response = requester.search(params);

        return response
                .then()
                .statusCode(SC_OK)
                .extract()
                .as(ListOrdersResponse.class);
    }
}