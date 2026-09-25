package apiTests.orders;

import apiParts.assertions.ModelAssertions;
import apiParts.models.EncounterType;
import apiParts.models.Location;
import apiParts.models.encounter.CreateEncounterRequest;
import apiParts.models.encounter.CreateEncounterResponse;
import apiParts.models.encounter.Ref;
import apiParts.models.order.DrugOrderResponse;
import apiParts.models.search.SearchResult;
import apiParts.skelethon.endpoints.Endpoint;
import apiParts.skelethon.requests.crud.CrudRequester;
import apiParts.skelethon.requests.crud.SuccessfulCrudRequester;
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
        // create a patient with a standard drug order encounter
        String patientUUID = SessionStorage.getPatient().getUuid();

        CreateEncounterRequest drugOrderRequest = AdminSteps.drugOrderEncounterRequest(patientUUID);
        CreateEncounterResponse encounter = new SuccessfulCrudRequester<CreateEncounterResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.ENCOUNTER_POST,
                ResponseSpecs.requestReturnsCreated())
                .create(drugOrderRequest);

        softly.assertThat(encounter.getOrders())
                .as("created drug order")
                .hasSize(1);
        String orderUUID = encounter.getOrders().get(0).getUuid();

        // verify the order was actually persisted and matches what was sent
        List<DrugOrderResponse> drugOrders = AdminSteps.fetchDrugOrders(patientUUID).results();
        softly.assertThat(drugOrders)
                .as("created drug order is retrievable via GET /order")
                .hasSize(1);
        DrugOrderResponse savedOrder = drugOrders.get(0);
        softly.assertThat(savedOrder.getUuid())
                .as("created drug order uuid")
                .isEqualTo(orderUUID);

        ModelAssertions.assertThatModels(softly, drugOrderRequest.getOrders(), List.of(savedOrder))
                .as("saved drug order")
                .match();
    }

    @Test
    public void adminCanCreateLabOrder() {
        // create a patient with a lab order (Alkaline phosphatase test) encounter
        String patientUUID = SessionStorage.getPatient().getUuid();

        CreateEncounterRequest labOrderRequest = AdminSteps.labOrderEncounterRequest(patientUUID);
        CreateEncounterResponse labEncounter = new SuccessfulCrudRequester<CreateEncounterResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.ENCOUNTER_POST,
                ResponseSpecs.requestReturnsCreated())
                .create(labOrderRequest);

        CreateEncounterResponse expectedLabEncounter = new CreateEncounterResponse();
        expectedLabEncounter.setPatient(Ref.of(patientUUID));
        expectedLabEncounter.setLocation(Ref.of(Location.INPATIENT_WARD.getUuid()));
        expectedLabEncounter.setEncounterType(Ref.of(EncounterType.ORDER.getUuid()));

        ModelAssertions.assertMatchesExpected(softly, labEncounter, expectedLabEncounter, "lab order encounter");

        softly.assertThat(labEncounter.getObs())
                .as("lab encounter obs")
                .isEmpty();
        softly.assertThat(labEncounter.getVoided())
                .as("lab encounter voided")
                .isFalse();
        softly.assertThat(labEncounter.getOrders())
                .as("lab encounter orders")
                .hasSize(1);
        softly.assertThat(labEncounter.getOrders().get(0).getDisplay())
                .as("lab order display")
                .isEqualTo("Alkaline phosphatase");

        // verify the order was actually persisted
        String orderUUID = labEncounter.getOrders().get(0).getUuid();
        SearchResult<DrugOrderResponse> patientOrders = AdminSteps.fetchTestOrders(patientUUID);

        softly.assertThat(patientOrders.results())
                .as("created lab order is retrievable via GET /order")
                .anyMatch(order -> order.getUuid().equals(orderUUID));
    }

    @Test
    @DisplayName("auth required to create order")
    public void authRequiredToCreateOrder() {
        String patientUUID = SessionStorage.getPatient().getUuid();
        List<DrugOrderResponse> before = AdminSteps.fetchTestOrders(patientUUID).results();

        CreateEncounterRequest labOrderRequest = AdminSteps.labOrderEncounterRequest(patientUUID);

        new CrudRequester(
                RequestSpecs.unAuthSpec(),
                Endpoint.ENCOUNTER_POST,
                ResponseSpecs.requestReturnsBadRequest())
                .create(labOrderRequest);

        ModelAssertions.assertUnchanged(softly, before, AdminSteps.fetchTestOrders(patientUUID).results(),
                "test orders after unauthorized create");
    }

    @Test
    public void adminCannotCreateOrderWithoutRequiredConcept() {
        String patientUUID = SessionStorage.getPatient().getUuid();
        List<DrugOrderResponse> before = AdminSteps.fetchTestOrders(patientUUID).results();

        // concept is required for a test order and is intentionally omitted here
        CreateEncounterRequest request = AdminSteps.labOrderRequestWithoutConcept(patientUUID);

        new CrudRequester(
                RequestSpecs.adminSpec(),
                Endpoint.ENCOUNTER_POST,
                ResponseSpecs.requestReturnsInvalidSubmission("concept"))
                .create(request);

        ModelAssertions.assertUnchanged(softly,
                before,
                AdminSteps.fetchTestOrders(patientUUID).results(),
                "test orders after create without concept");
    }

    @Test
    @DisplayName("admin cannot create order without careSetting")
    public void adminCannotCreateOrderWithoutRequiredCareSetting() {
        String patientUUID = SessionStorage.getPatient().getUuid();
        List<DrugOrderResponse> before = AdminSteps.fetchTestOrders(patientUUID).results();

        // careSetting is required for an order and is intentionally omitted here
        CreateEncounterRequest request = AdminSteps.labOrderRequestWithoutCareSetting(patientUUID);

        new CrudRequester(
                RequestSpecs.adminSpec(),
                Endpoint.ENCOUNTER_POST,
                ResponseSpecs.requestReturnsServerError())
                .create(request);

        ModelAssertions.assertUnchanged(softly,
                before,
                AdminSteps.fetchTestOrders(patientUUID).results(),
                "test orders after create without careSetting");
    }

}
