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
        softly.assertThat(savedEncounter.getOrders())
                .extracting(Ref::getUuid)
                .as("persisted drug order references")
                .hasSize(drugOrderRequest.getOrders().size())
                .containsExactlyInAnyOrderElementsOf(
                        encounter.getOrders().stream().map(Ref::getUuid).toList());

        softly.assertThat(encounter.getOrders())
                .as("created drug order")
                .hasSize(drugOrderRequest.getOrders().size());
        String orderUUID = encounter.getOrders().get(0).getUuid();

        // verify the order was actually persisted and matches what was sent
        List<DrugOrderResponse> drugOrders = AdminSteps.fetchDrugOrders(patientUUID).results();
        softly.assertThat(drugOrders)
                .as("created drug order is retrievable via GET /order")
                .hasSize(drugOrderRequest.getOrders().size());
        DrugOrderResponse savedOrder = drugOrders.get(0);
        softly.assertThat(savedOrder.getUuid())
                .as("created drug order uuid")
                .isEqualTo(orderUUID);

        ModelAssertions.assertThatModels(drugOrderRequest.getOrders(), List.of(savedOrder))
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
        softly.assertThat(savedLabEncounter.getOrders())
                .extracting(Ref::getUuid)
                .as("persisted lab order references")
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
        // Verify the order was actually persisted and matches the generated request.
        String orderUUID = labEncounter.getOrders().get(0).getUuid();
        DrugOrderResponse savedOrder = AdminSteps.fetchTestOrders(patientUUID)
                .requireOne(order -> order.getUuid().equals(orderUUID), "created lab order");
        softly.assertThat(savedOrder.getPatient().getUuid())
                .as("saved lab order patient")
                .isEqualTo(patientUUID);
        softly.assertThat(savedOrder.getConcept().getUuid())
                .as("saved lab order concept")
                .isEqualTo(labOrder.getConcept().getUuid());
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