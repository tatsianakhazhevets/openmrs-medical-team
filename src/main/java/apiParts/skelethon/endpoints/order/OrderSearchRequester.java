package apiParts.skelethon.endpoints.order;

import apiParts.models.order.OrderSearchParams;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import lombok.RequiredArgsConstructor;

import java.util.Objects;

import static io.restassured.RestAssured.given;

@RequiredArgsConstructor
public class OrderSearchRequester implements OrderSearchEndpoint {

    private static final String ORDER_ENDPOINT = "/order";

    private final RequestSpecification requestSpecification;

    @Override
    public Response search(OrderSearchParams params) {
        Objects.requireNonNull(
                params,
                "Order search parameters must not be null"
        );

        RequestSpecification request = given()
                .spec(requestSpecification);

        addQueryParameter(request, "patient", params.getPatient());
        addQueryParameter(request, "caresetting", params.getCareSetting());
        addQueryParameter(request, "limit", params.getLimit());
        addQueryParameter(request, "startIndex", params.getStartIndex());
        addQueryParameter(request, "v", params.getRepresentation());

        return request
                .when()
                .get(ORDER_ENDPOINT);
    }

    private void addQueryParameter(
            RequestSpecification request,
            String name,
            Object value
    ) {
        if (value != null) {
            request.queryParam(name, value);
        }
    }
}
