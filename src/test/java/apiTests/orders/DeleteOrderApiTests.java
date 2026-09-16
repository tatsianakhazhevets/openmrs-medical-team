package apiTests.orders;

import apiParts.models.encounter.CreateEncounterResponse;
import apiParts.skelethon.endpoints.Endpoint;
import apiParts.skelethon.requests.common.CrudRequester;
import apiParts.specs.RequestSpecs;
import apiParts.specs.ResponseSpecs;
import apiParts.steps.AdminSteps;
import apiTests.BaseTest;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class DeleteOrderApiTests extends BaseTest {

    @Test
    public void adminCanDeleteOrder() {
        var patientResponse = AdminSteps.createPatient();
        String patientUUID = patientResponse.getUuid();

        CreateEncounterResponse encounter = AdminSteps.createLabOrderEncounter(patientUUID);
        String orderUUID = encounter.getOrders().get(0).getUuid();

        var ordersBeforeDelete = AdminSteps.fetchTestOrders(patientUUID);
        assertThat(ordersBeforeDelete.getResults())
                .as("order is present before delete")
                .anyMatch(order -> order.getUuid().equals(orderUUID));

        new CrudRequester(
                RequestSpecs.adminSpec(),
                Endpoint.ORDER_DELETE,
                ResponseSpecs.requestReturnsNoContent())
                .delete(orderUUID);

        var ordersAfterDelete = AdminSteps.fetchTestOrders(patientUUID);
        softly.assertThat(ordersAfterDelete.getResults())
                .as("voided order is no longer returned in GET /order")
                .noneMatch(order -> order.getUuid().equals(orderUUID));
    }

    // Purging an order that is referenced by an encounter is not supported and results in a server error
    @Test
    public void adminCannotPurgeOrderReferencedByEncounter() {
        var patientResponse = AdminSteps.createPatient();
        String patientUUID = patientResponse.getUuid();

        CreateEncounterResponse encounter = AdminSteps.createLabOrderEncounter(patientUUID);
        String orderUUID = encounter.getOrders().get(0).getUuid();

        new CrudRequester(
                RequestSpecs.adminSpec(),
                Endpoint.ORDER_DELETE,
                ResponseSpecs.requestReturnsServerError())
                .delete(orderUUID, Map.of("purge", true));

        var ordersAfterFailedPurge = AdminSteps.fetchTestOrders(patientUUID);
        softly.assertThat(ordersAfterFailedPurge.getResults())
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
    public void unauthorizedUserCannotDeleteOrder() {
        var patientResponse = AdminSteps.createPatient();
        String patientUUID = patientResponse.getUuid();

        CreateEncounterResponse encounter = AdminSteps.createLabOrderEncounter(patientUUID);
        String orderUUID = encounter.getOrders().get(0).getUuid();

        new CrudRequester(
                RequestSpecs.unAuthSpec(),
                Endpoint.ORDER_DELETE,
                ResponseSpecs.requestReturnsUnauthorized())
                .delete(orderUUID);

        var ordersAfterUnauthorizedDelete = AdminSteps.fetchTestOrders(patientUUID);
        softly.assertThat(ordersAfterUnauthorizedDelete.getResults())
                .as("order still present after unauthorized delete attempt")
                .anyMatch(order -> order.getUuid().equals(orderUUID));
    }

}
