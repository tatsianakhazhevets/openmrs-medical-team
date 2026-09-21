package apiTests.orders;

import apiParts.skelethon.endpoints.Endpoint;
import apiParts.skelethon.requests.common.CrudRequester;
import apiParts.specs.RequestSpecs;
import apiParts.specs.ResponseSpecs;
import apiParts.steps.AdminSteps;
import apiTests.BaseTest;
import common.annotations.CreateOrder;
import common.annotations.CreatePatient;
import common.storages.SessionStorage;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static common.annotations.CreateOrder.Type.LAB;
import static org.assertj.core.api.Assertions.assertThat;

public class DeleteOrderApiTests extends BaseTest {

    @Test
    @CreatePatient
    @CreateOrder(LAB)
    public void adminCanDeleteOrder() {
        String patientUUID = SessionStorage.getPatient().getUuid();
        String orderUUID = SessionStorage.getOrderUuid();

        var ordersBeforeDelete = AdminSteps.fetchTestOrders(patientUUID);
        assertThat(ordersBeforeDelete.results())
                .as("order is present before delete")
                .anyMatch(order -> order.getUuid().equals(orderUUID));

        new CrudRequester(
                RequestSpecs.adminSpec(),
                Endpoint.ORDER_DELETE,
                ResponseSpecs.requestReturnsNoContent())
                .delete(orderUUID);

        var ordersAfterDelete = AdminSteps.fetchTestOrders(patientUUID);
        softly.assertThat(ordersAfterDelete.results())
                .as("voided order is no longer returned in GET /order")
                .noneMatch(order -> order.getUuid().equals(orderUUID));
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

        var ordersAfterFailedPurge = AdminSteps.fetchTestOrders(patientUUID);
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

        var ordersAfterUnauthorizedDelete = AdminSteps.fetchTestOrders(patientUUID);
        softly.assertThat(ordersAfterUnauthorizedDelete.results())
                .as("order still present after unauthorized delete attempt")
                .anyMatch(order -> order.getUuid().equals(orderUUID));
    }

}
