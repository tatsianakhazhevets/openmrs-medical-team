package apiTests.orders;

import apiParts.assertions.ModelAssertions;
import apiParts.generators.DrugOrderGenerator;
import apiParts.generators.RandomModelGenerator;
import apiParts.models.encounter.EncounterType;
import apiParts.models.Location;
import apiParts.models.encounter.CreateEncounterRequest;
import apiParts.models.encounter.EncounterResponse;
import apiParts.models.Ref;
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

        softly.assertThat(orders.results())
                .as("one created drug order returned")
                .hasSize(request.getOrders().size());
        softly.assertThat(orders.results().get(0).getUuid())
                .as("created order is retrievable")
                .isEqualTo(encounter.getOrders().get(0).getUuid());
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
        String orderUUID = drugEncounter.getOrders().get(0).getUuid();

        List<DrugOrderResponse> drugOrders = AdminSteps.fetchDrugOrders(patientUUID).results();
        softly.assertThat(drugOrders)
                .as("created drug order is retrievable")
                .hasSize(drugOrderRequest.getOrders().size());
        DrugOrderResponse savedOrder = drugOrders.get(0);
        softly.assertThat(savedOrder.getUuid())
                .as("created order uuid")
                .isEqualTo(orderUUID);

        ModelAssertions.assertThatModels(drugOrderRequest.getOrders(), List.of(savedOrder))
                .as("saved drug order")
                .match();

        // Add a lab order encounter for the same patient.
        TestOrder labOrder = RandomModelGenerator.generate(TestOrder.class);
        CreateEncounterRequest labOrderRequest = RandomModelGenerator.generate(CreateEncounterRequest.class);
        labOrderRequest.setLocation(Location.INPATIENT_WARD);
        labOrderRequest.setOrders(List.of(labOrder));
        EncounterResponse labEncounter = AdminSteps.createEncounter(labOrderRequest);
        EncounterResponse savedLabEncounter = AdminSteps.getEncounter(labEncounter.getUuid());
        softly.assertThat(savedLabEncounter.getOrders())
                .extracting(Ref::getUuid)
                .as("lab order references persisted on encounter")
                .hasSize(labOrderRequest.getOrders().size())
                .containsExactlyInAnyOrderElementsOf(
                        labEncounter.getOrders().stream().map(Ref::getUuid).toList());

        EncounterResponse expectedLabEncounter = new EncounterResponse();
        expectedLabEncounter.setPatient(Ref.of(patientUUID));
        expectedLabEncounter.setLocation(Ref.of(Location.INPATIENT_WARD.getUuid()));
        expectedLabEncounter.setEncounterType(Ref.of(EncounterType.ORDER.getUuid()));

        ModelAssertions.assertMatchesExpected(labEncounter, expectedLabEncounter, "lab order encounter");

        softly.assertThat(labEncounter.getObs())
                .as("lab encounter obs")
                .isEmpty();
        softly.assertThat(labEncounter.getVoided())
                .as("lab encounter voided")
                .isFalse();
        softly.assertThat(labEncounter.getOrders())
                .as("lab encounter orders")
                .hasSize(labOrderRequest.getOrders().size());
        softly.assertThat(labEncounter.getOrders().get(0).getDisplay())
                .as("lab order display")
                .isNotBlank();

        DrugOrderResponse savedLabOrder = AdminSteps.fetchTestOrders(patientUUID)
                .requireOne(order -> order.getUuid().equals(labEncounter.getOrders().get(0).getUuid()),
                        "created lab order");
        softly.assertThat(savedLabOrder.getConcept().getUuid())
                .as("saved lab order concept")
                .isEqualTo(labOrder.getConcept().getUuid());
    }
}