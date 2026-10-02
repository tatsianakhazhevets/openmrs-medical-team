package apiTests.orders;

import apiParts.assertions.ModelAssertions;
import apiParts.assertions.OrderAssertions;
import apiParts.generators.DrugOrderGenerator;
import apiParts.generators.RandomModelGenerator;
import apiParts.models.Location;
import apiParts.models.encounter.CreateEncounterRequest;
import apiParts.models.encounter.EncounterResponse;
import apiParts.models.order.DrugOrder;
import apiParts.models.order.DrugOrderResponse;
import apiParts.models.order.OrderSearchParams;
import apiParts.models.order.TestOrder;
import apiParts.models.search.SearchResult;
import apiParts.skelethon.endpoints.Endpoint;
import apiParts.skelethon.requests.search.SearchRequester;
import apiParts.specs.RequestSpecs;
import apiParts.specs.ResponseSpecs;
import apiParts.steps.AdminSteps;
import apiTests.BaseTest;
import common.annotations.CreatePatient;
import common.storages.SessionStorage;
import org.junit.jupiter.api.Test;

import java.util.List;

@CreatePatient
public class GetOrderApiTests extends BaseTest {

    @Test
    public void adminCanFetchListOfOrders() {
        String patientUUID = SessionStorage.getPatient().getUuid();
        DrugOrder drugOrder = DrugOrderGenerator.generateDrugOrder();
        CreateEncounterRequest request = DrugOrderGenerator.generateEncounterRequest(drugOrder);
        EncounterResponse encounter = AdminSteps.createEncounter(request);
        SearchResult<DrugOrderResponse> orders = AdminSteps.fetchDrugOrders(patientUUID);

        OrderAssertions.assertDrugOrderSaved(request, encounter, orders.results(), "drug order");
        ModelAssertions.assertThatModels(request.getOrders(), orders.results())
                .as("drug orders returned for patient")
                .match();
    }

    @Test
    public void authRequiredToFetchListOfOrders() {
        String patientUUID = SessionStorage.getPatient().getUuid();
        var ordersBefore = AdminSteps.fetchDrugOrders(patientUUID).results();
        OrderSearchParams searchParams = AdminSteps.inpatientOrderSearchParams(patientUUID);

        new SearchRequester(
                RequestSpecs.unAuthSpec(),
                Endpoint.LIST_ORDERS_GET,
                ResponseSpecs.requestReturnsUnauthorized())
                .search(searchParams);

        ModelAssertions.assertUnchanged(ordersBefore, AdminSteps.fetchDrugOrders(patientUUID).results(),
                "drug orders after unauthorized list request");
    }

    @Test
    public void adminCanCheckSpecificOrderDetails() {
        String patientUUID = SessionStorage.getPatient().getUuid();
        DrugOrder drugOrder = DrugOrderGenerator.generateDrugOrder();
        CreateEncounterRequest drugOrderRequest = DrugOrderGenerator.generateEncounterRequest(drugOrder);
        EncounterResponse drugEncounter = AdminSteps.createEncounter(drugOrderRequest);
        List<DrugOrderResponse> drugOrders = AdminSteps.fetchDrugOrders(patientUUID).results();
        OrderAssertions.assertDrugOrderSaved(drugOrderRequest, drugEncounter, drugOrders, "saved drug order");
        ModelAssertions.assertThatModels(drugOrderRequest.getOrders(), drugOrders)
                .as("saved drug order")
                .match();

        // Add a lab order encounter for the same patient.
        TestOrder labOrder = RandomModelGenerator.generate(TestOrder.class);
        CreateEncounterRequest labOrderRequest = RandomModelGenerator.generate(CreateEncounterRequest.class);
        labOrderRequest.setLocation(Location.INPATIENT_WARD);
        labOrderRequest.setOrders(List.of(labOrder));
        EncounterResponse labEncounter = AdminSteps.createEncounter(labOrderRequest);
        EncounterResponse savedLabEncounter = AdminSteps.getEncounter(labEncounter.getUuid());

        DrugOrderResponse savedLabOrder = AdminSteps.fetchTestOrders(patientUUID)
                .requireOne(order -> order.getUuid().equals(labEncounter.getOrders().get(0).getUuid()),
                        "created lab order");
        OrderAssertions.assertLabOrderDetails(patientUUID, labOrder, labOrderRequest, labEncounter,
                savedLabEncounter, savedLabOrder);
        ModelAssertions.assertThatModels(labOrderRequest, labEncounter)
                .as("lab order encounter")
                .match();
    }
}