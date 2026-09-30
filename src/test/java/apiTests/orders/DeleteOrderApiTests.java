package apiTests.orders;

import apiParts.assertions.ModelAssertions;
import apiParts.generators.RandomModelGenerator;
import apiParts.models.Location;
import apiParts.models.encounter.CreateEncounterRequest;
import apiParts.models.encounter.EncounterResponse;
import apiParts.models.order.OrderDeleteParams;
import apiParts.models.order.TestOrder;
import apiParts.skelethon.endpoints.Endpoint;
import apiParts.skelethon.requests.crud.CrudRequester;
import apiParts.specs.RequestSpecs;
import apiParts.specs.ResponseSpecs;
import apiParts.steps.AdminSteps;
import apiParts.models.order.DrugOrderResponse;
import apiParts.models.search.SearchResult;
import apiTests.BaseTest;
import common.annotations.CreatePatient;
import common.storages.SessionStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

@CreatePatient
public class DeleteOrderApiTests extends BaseTest {
    private String patientUUID;
    private String orderUUID;

    @BeforeEach
    void setUp() {
        patientUUID = SessionStorage.getPatient().getUuid();

        TestOrder order = RandomModelGenerator.generate(TestOrder.class);

        CreateEncounterRequest request = RandomModelGenerator.generate(CreateEncounterRequest.class);
        request.setLocation(Location.INPATIENT_WARD);
        request.setOrders(List.of(order));
        EncounterResponse encounter = AdminSteps.createEncounter(request);
        orderUUID = encounter.getOrders().get(0).getUuid();
    }

    @Test
    public void adminCanDeleteOrder() {
        new CrudRequester(
                RequestSpecs.adminSpec(),
                Endpoint.ORDER_DELETE,
                ResponseSpecs.requestReturnsNoContent())
                .delete(orderUUID, OrderDeleteParams.builder()
                        .reason(RandomModelGenerator.randomSentence())
                        .build()
                        .toQueryParams());

        softly.assertThat(AdminSteps.fetchTestOrders(patientUUID).results())
                .as("deleted order is no longer returned")
                .noneMatch(order -> order.getUuid().equals(orderUUID));
    }

    // Purging an order that is referenced by an encounter is not supported and results in a server error
    @Test
    public void adminCannotPurgeOrderReferencedByEncounter() {
        new CrudRequester(
                RequestSpecs.adminSpec(),
                Endpoint.ORDER_DELETE,
                ResponseSpecs.requestReturnsServerError())
                .delete(orderUUID, OrderDeleteParams.builder().purge(true).build().toQueryParams());

        SearchResult<DrugOrderResponse> ordersAfterFailedPurge = AdminSteps.fetchTestOrders(patientUUID);
        softly.assertThat(ordersAfterFailedPurge.results())
                .as("order still present after failed purge attempt")
                .anyMatch(order -> order.getUuid().equals(orderUUID));
    }

    @Test
    public void adminCannotDeleteNonExistentOrder() {
        var ordersBefore = AdminSteps.fetchTestOrders(patientUUID).results();

        new CrudRequester(
                RequestSpecs.adminSpec(),
                Endpoint.ORDER_DELETE,
                ResponseSpecs.requestReturnsNotFound())
                .delete(UUID.randomUUID().toString());

        ModelAssertions.assertUnchanged(
                ordersBefore,
                AdminSteps.fetchTestOrders(patientUUID).results(),
                "test orders after deleting a non-existent order");
    }

    @Test
    public void unauthorizedUserCannotDeleteOrder() {
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
