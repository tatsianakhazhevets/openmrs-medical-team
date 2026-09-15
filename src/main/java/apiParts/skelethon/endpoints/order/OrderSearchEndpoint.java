package apiParts.skelethon.endpoints.order;
import apiParts.models.order.OrderSearchParams;
import io.restassured.response.Response;

public interface OrderSearchEndpoint {
    Response search(OrderSearchParams params);
}
