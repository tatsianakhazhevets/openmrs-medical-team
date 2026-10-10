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
import apiParts.models.order.TestOrder;
import apiParts.skelethon.endpoints.Endpoint;
import apiParts.skelethon.requests.crud.CrudRequester;
import apiParts.specs.RequestSpecs;
import apiParts.specs.ResponseSpecs;
import apiParts.steps.AdminSteps;
import apiTests.BaseTest;
import common.annotations.CreatePatient;
import common.storages.SessionStorage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CreatePatient
public class CreateOrderApiTests extends BaseTest {

    @Test
    public void adminCanCreateDrugOrder() {
        String patientUUID = SessionStorage.getPatient().getUuid();
        DrugOrder drugOrder = DrugOrderGenerator.generateDrugOrder();
        CreateEncounterRequest drugOrderRequest = DrugOrderGenerator.generateEncounterRequest(drugOrder);
        EncounterResponse encounter = AdminSteps.createEncounter(drugOrderRequest);
        EncounterResponse savedEncounter = AdminSteps.getEncounter(encounter.getUuid());
        OrderAssertions.assertPersistedOrderReferences(drugOrderRequest, encounter, savedEncounter,
                "persisted drug order references");
        List<DrugOrderResponse> drugOrders = AdminSteps.fetchDrugOrders(patientUUID).results();
        OrderAssertions.assertDrugOrderSaved(drugOrderRequest, encounter, drugOrders, "created drug order");
        ModelAssertions.assertThatModels(drugOrderRequest.getOrders(), drugOrders)
                .as("saved drug order")
                .match();
    }

    @Test
    public void adminCanCreateLabOrder() {
        String patientUUID = SessionStorage.getPatient().getUuid();

        TestOrder labOrder = RandomModelGenerator.generate(TestOrder.class);
        CreateEncounterRequest labOrderRequest = RandomModelGenerator.generate(CreateEncounterRequest.class);
        labOrderRequest.setLocation(Location.INPATIENT_WARD);
        labOrderRequest.setOrders(List.of(labOrder));
        EncounterResponse labEncounter = AdminSteps.createEncounter(labOrderRequest);
        EncounterResponse savedLabEncounter = AdminSteps.getEncounter(labEncounter.getUuid());
        String orderUUID = labEncounter.getOrders().get(0).getUuid();
        DrugOrderResponse savedOrder = AdminSteps.fetchTestOrders(patientUUID)
                .requireOne(order -> order.getUuid().equals(orderUUID), "created lab order");
        OrderAssertions.assertLabOrderDetails(patientUUID, labOrder, labOrderRequest, labEncounter,
                savedLabEncounter, savedOrder);
        ModelAssertions.assertThatModels(labOrderRequest, labEncounter)
                .as("lab order encounter")
                .match();
    }

    @Test
    @DisplayName("auth required to create order")
    public void authRequiredToCreateOrder() {
        String patientUUID = SessionStorage.getPatient().getUuid();
        List<DrugOrderResponse> before = AdminSteps.fetchTestOrders(patientUUID).results();

        TestOrder labOrder = RandomModelGenerator.generate(TestOrder.class);
        CreateEncounterRequest labOrderRequest = RandomModelGenerator.generate(CreateEncounterRequest.class);
        labOrderRequest.setLocation(Location.INPATIENT_WARD);
        labOrderRequest.setOrders(List.of(labOrder));

        new CrudRequester(
                RequestSpecs.unAuthSpec(),
                Endpoint.ENCOUNTER_POST,
                ResponseSpecs.requestReturnsBadRequest())
                .create(labOrderRequest);

        ModelAssertions.assertUnchanged(before, AdminSteps.fetchTestOrders(patientUUID).results(),
                "test orders after unauthorized create");
    }

    @Test
    public void adminCannotCreateOrderWithoutRequiredConcept() {
        String patientUUID = SessionStorage.getPatient().getUuid();
        List<DrugOrderResponse> before = AdminSteps.fetchTestOrders(patientUUID).results();

        // concept is required for a test order and is intentionally omitted here
        TestOrder labOrder = RandomModelGenerator.generate(TestOrder.class);
        labOrder.setConcept(null);
        CreateEncounterRequest request = RandomModelGenerator.generate(CreateEncounterRequest.class);
        request.setLocation(Location.INPATIENT_WARD);
        request.setOrders(List.of(labOrder));

        new CrudRequester(
                RequestSpecs.adminSpec(),
                Endpoint.ENCOUNTER_POST,
                ResponseSpecs.requestReturnsInvalidSubmission(TestOrder.CONCEPT_FIELD_NAME))
                .create(request);

        ModelAssertions.assertUnchanged(before,
                AdminSteps.fetchTestOrders(patientUUID).results(),
                "test orders after create without concept");
    }

    @Test
    @DisplayName("admin cannot create order without careSetting")
    public void adminCannotCreateOrderWithoutRequiredCareSetting() {
        String patientUUID = SessionStorage.getPatient().getUuid();
        List<DrugOrderResponse> before = AdminSteps.fetchTestOrders(patientUUID).results();

        // careSetting is required for an order and is intentionally omitted here
        TestOrder labOrder = RandomModelGenerator.generate(TestOrder.class);
        labOrder.setCareSetting(null);
        CreateEncounterRequest request = RandomModelGenerator.generate(CreateEncounterRequest.class);
        request.setLocation(Location.INPATIENT_WARD);
        request.setOrders(List.of(labOrder));

        new CrudRequester(
                RequestSpecs.adminSpec(),
                Endpoint.ENCOUNTER_POST,
                ResponseSpecs.requestReturnsServerError())
                .create(request);

        ModelAssertions.assertUnchanged(before,
                AdminSteps.fetchTestOrders(patientUUID).results(),
                "test orders after create without careSetting");
    }
}