package apiTests.orders;

import apiParts.skelethon.endpoints.Endpoint;
import apiParts.skelethon.requests.crud.CrudRequester;
import apiParts.specs.RequestSpecs;
import apiParts.specs.ResponseSpecs;
import apiParts.steps.AdminSteps;
import apiParts.models.order.DrugOrderResponse;
import apiParts.models.search.SearchResult;
import apiTests.BaseTest;
import common.annotations.CreateOrder;
import common.annotations.CreatePatient;
import common.storages.SessionStorage;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static common.annotations.CreateOrder.Type.LAB;

public class DeleteOrderApiTests extends BaseTest {
    private static final String VOID_REASON = "Automated test cleanup";

    @Test
    @CreatePatient
    @CreateOrder(LAB)
    public void adminCanDeleteOrder() {
        String orderUUID = SessionStorage.getOrderUuid();

        new CrudRequester(
                RequestSpecs.adminSpec(),
                Endpoint.ORDER_DELETE,
                ResponseSpecs.requestReturnsNoContent())
                .delete(orderUUID, Map.of("reason", VOID_REASON));
    }

    // Purging an order that is referenced by an encounter is not supported and results in a server error
    @Test
    @CreatePatient
    @CreateOrder(LAB)
    public void adminCannotPurgeOrderReferencedByEncounter() {
        String patientUUID = SessionStorage.getPatient().getUuid();
        String orderUUID = SessionStorage.getOrderUuid();

        new CrudRequester(
                RequestSpecs.adminSpec(),
                Endpoint.ORDER_DELETE,
                ResponseSpecs.requestReturnsServerError())
                .delete(orderUUID, Map.of("purge", true));

        SearchResult<DrugOrderResponse> ordersAfterFailedPurge = AdminSteps.fetchTestOrders(patientUUID);
        softly.assertThat(ordersAfterFailedPurge.results())
                .as("order still present after failed purge attempt")
                .anyMatch(order -> order.getUuid().equals(orderUUID));
    }

    @Test
    public void adminCannotDeleteNonExistentOrder() {
        new CrudRequester(
                RequestSpecs.adminSpec(),
                Endpoint.ORDER_DELETE,
                ResponseSpecs.requestReturnsNotFound())
                .delete(UUID.randomUUID().toString());
    }

    @Test
    @CreatePatient
    @CreateOrder(LAB)
    public void unauthorizedUserCannotDeleteOrder() {
        String patientUUID = SessionStorage.getPatient().getUuid();
        String orderUUID = SessionStorage.getOrderUuid();

        new CrudRequester(
                RequestSpecs.unAuthSpec(),
                Endpoint.ORDER_DELETE,
                ResponseSpecs.requestReturnsUnauthorized())
                .delete(orderUUID);

        SearchResult<DrugOrderResponse> ordersAfterUnauthorizedDelete = AdminSteps.fetchTestOrders(patientUUID);
        softly.assertThat(ordersAfterUnauthorizedDelete.results())
                .as("order still present after unauthorized delete attempt")
                .anyMatch(order -> order.getUuid().equals(orderUUID));
    }

}
