package apiTests.orders;

import apiParts.assertions.ModelAssertions;
import apiParts.assertions.OrderAssertions;
import apiParts.generators.RandomModelGenerator;
import apiParts.models.Location;
import apiParts.models.encounter.CreateEncounterRequest;
import apiParts.models.encounter.EncounterResponse;
import apiParts.models.order.OrderDeleteParams;
import apiParts.models.order.Drug;
import apiParts.models.order.TestOrder;
import apiParts.skelethon.endpoints.Endpoint;
import apiParts.skelethon.requests.crud.CrudRequester;
import apiParts.specs.RequestSpecs;
import apiParts.specs.ResponseSpecs;
import apiParts.steps.AdminSteps;
import apiParts.testdata.OrderTestData;
import common.storages.SessionStorage;
import apiParts.models.order.DrugOrderResponse;
import apiParts.models.search.SearchResult;
import apiTests.BaseTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.api.parallel.Isolated;

import java.util.List;
import java.util.UUID;

@Isolated("Concurrent order purge and void requests can corrupt the backend Hibernate persistence context")
@Execution(ExecutionMode.SAME_THREAD)
public class DeleteOrderApiTests extends BaseTest {
    private String patientUUID;
    private String orderUUID;

    private void setUpPatient(boolean withAddress) {
        var patient = withAddress ? AdminSteps.createPatientWithAddress() : AdminSteps.createPatient();
        SessionStorage.addPatients(List.of(patient));
        patientUUID = patient.getUuid();
    }

    private void setUpTestOrder() {
        TestOrder order = RandomModelGenerator.generate(TestOrder.class);
        CreateEncounterRequest request = RandomModelGenerator.generate(CreateEncounterRequest.class);
        request.setLocation(Location.INPATIENT_WARD);
        request.setOrders(List.of(order));
        EncounterResponse encounter = AdminSteps.createEncounter(request);
        orderUUID = encounter.getOrders().get(0).getUuid();
    }

    private void setUpDrugOrder() {
        var encounter = AdminSteps.createDrugOrderEncounter(patientUUID);
        orderUUID = encounter.getOrders().get(0).getUuid();
    }

    @Test
    @DisplayName("admin can cancel drug order")
    public void adminCanCancelDrugOrder() {
        setUpPatient(true);
        setUpDrugOrder();

        OrderAssertions.assertOrderPresent(AdminSteps.fetchDrugOrders(patientUUID).results(),
                orderUUID, "precondition: active order exists before cancellation");

        AdminSteps.createEncounter(OrderTestData.discontinueDrugOrderEncounterRequest(
                patientUUID, orderUUID, Drug.ASPIRIN_325MG, AdminSteps.getCurrentProviderUuid()));

        OrderAssertions.assertOrderAbsent(AdminSteps.fetchDrugOrders(patientUUID).results(),
                orderUUID, "cancelled order is no longer returned as active");
    }

    // Purging an order that is referenced by an encounter is not supported and results in a server error
    @Test
    public void adminCannotPurgeOrderReferencedByEncounter() {
        setUpPatient(false);
        setUpTestOrder();

        new CrudRequester(
                RequestSpecs.adminSpec(),
                Endpoint.ORDER_DELETE,
                ResponseSpecs.requestReturnsServerError())
                .delete(orderUUID, OrderDeleteParams.builder().purge(true).build().toQueryParams());

        SearchResult<DrugOrderResponse> ordersAfterFailedPurge = AdminSteps.fetchTestOrders(patientUUID);
        OrderAssertions.assertOrderPresent(ordersAfterFailedPurge.results(),
                orderUUID, "order still present after failed purge attempt");
    }

    @Test
    public void adminCannotDeleteNonExistentOrder() {
        setUpPatient(false);
        setUpTestOrder();
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
        setUpPatient(false);
        setUpTestOrder();

        new CrudRequester(
                RequestSpecs.unAuthSpec(),
                Endpoint.ORDER_DELETE,
                ResponseSpecs.requestReturnsUnauthorized())
                .delete(orderUUID);

        SearchResult<DrugOrderResponse> ordersAfterUnauthorizedDelete = AdminSteps.fetchTestOrders(patientUUID);
        OrderAssertions.assertOrderPresent(ordersAfterUnauthorizedDelete.results(),
                orderUUID, "order still present after unauthorized delete attempt");
    }
}