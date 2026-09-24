package apiTests.laboratory;

import apiParts.models.encounter.CreateEncounterResponse;
import apiParts.models.encounter.ObsResponse;
import apiParts.models.encounter.ObsStatus;
import apiParts.models.order.DiscontinueOrderRequest;
import apiParts.models.order.FulfillerDetailsRequest;
import apiParts.models.order.FulfillerStatus;
import apiParts.models.order.LabTestConcept;
import apiParts.models.order.Order;
import apiParts.generators.RandomModelGenerator;
import apiParts.skelethon.endpoints.Endpoint;
import apiParts.skelethon.requests.crud.CrudRequester;
import apiParts.skelethon.requests.crud.SuccessfulCrudRequester;
import apiParts.skelethon.requests.action.ActionRequester;
import apiParts.specs.RequestSpecs;
import apiParts.specs.ResponseSpecs;
import apiParts.steps.AdminSteps;
import apiTests.BaseTest;
import common.annotations.CreateOrder;
import common.annotations.CreatePatient;
import common.storages.SessionStorage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static common.annotations.CreateOrder.Type.LAB;

@CreatePatient
@CreateOrder(LAB)
public class LaboratoryApiTests extends BaseTest {

    @Test
    public void adminCanProcessLabTestOrderFromReceivedToCompleted() {
        String patientUUID = SessionStorage.getPatient().getUuid();
        String orderUUID = SessionStorage.getOrderUuid();
        String encounterUUID = SessionStorage.getOrderEncounter().getUuid();
        // Alkaline phosphatase is not configured as a "precise" concept, so the value must be a whole number
        Double resultValue = (double) RandomModelGenerator.randomInt(1, 270);
        String completionComment = RandomModelGenerator.randomSentence();

        // 1) laboratory starts processing the sample
        AdminSteps.markOrderFulfillerStatus(orderUUID, FulfillerStatus.IN_PROGRESS, null);
        softly.assertThat(AdminSteps.fetchOrder(orderUUID).getFulfillerStatus())
                .as("order fulfillerStatus after starting the test")
                .isEqualTo(FulfillerStatus.IN_PROGRESS);

        // 2) laboratory enters the result against the order
        var resultRequest = AdminSteps.labResultEncounterRequest(orderUUID, resultValue);

        CreateEncounterResponse resultEncounter = new SuccessfulCrudRequester<CreateEncounterResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.ENCOUNTER_POST,
                ResponseSpecs.requestReturnsOk())
                .update(encounterUUID, resultRequest);

        softly.assertThat(resultEncounter.getObs())
                .as("result obs added to the order encounter")
                .hasSize(1);
        String obsUUID = resultEncounter.getObs().get(0).getUuid();

        ObsResponse savedObs = AdminSteps.fetchObs(patientUUID, obsUUID);
        softly.assertThat(savedObs.getConcept().getUuid())
                .as("result concept")
                .isEqualTo(LabTestConcept.ALKALINE_PHOSPHATASE.getUuid());
        softly.assertThat(savedObs.getOrder().getUuid())
                .as("result is linked to the tested order")
                .isEqualTo(orderUUID);
        softly.assertThat(savedObs.getStatus())
                .as("result status")
                .isEqualTo(ObsStatus.FINAL);
        softly.assertThat(savedObs.getValue())
                .as("result value")
                .isEqualTo(resultValue);

        // 3) the tested order is discontinued once a result is captured
        Order discontinuation = AdminSteps.discontinueOrder(orderUUID, patientUUID, encounterUUID);

        softly.assertThat(discontinuation.getAction())
                .as("discontinuation order action")
                .isEqualTo("DISCONTINUE");
        softly.assertThat(discontinuation.getPreviousOrder().getUuid())
                .as("discontinuation references the tested order")
                .isEqualTo(orderUUID);
        softly.assertThat(AdminSteps.fetchOrder(orderUUID).getDateStopped())
                .as("tested order is stopped after discontinuation")
                .isNotNull();

        // 4) laboratory marks the order as completed with a comment
        AdminSteps.markOrderFulfillerStatus(orderUUID, FulfillerStatus.COMPLETED, completionComment);

        Order completedOrder = AdminSteps.fetchOrder(orderUUID);
        softly.assertThat(completedOrder.getFulfillerStatus())
                .as("order fulfillerStatus after completion")
                .isEqualTo(FulfillerStatus.COMPLETED);
        softly.assertThat(completedOrder.getFulfillerComment())
                .as("order fulfillerComment after completion")
                .isEqualTo(completionComment);
    }

    @Test
    @DisplayName("auth required to update order fulfiller status")
    public void authRequiredToMarkFulfillerStatus() {
        String orderUUID = SessionStorage.getOrderUuid();
        FulfillerDetailsRequest request = AdminSteps.fulfillerDetailsRequest(FulfillerStatus.IN_PROGRESS, null);

        new ActionRequester(
                RequestSpecs.unAuthSpec(),
                Endpoint.ORDER_FULFILLER_DETAILS,
                ResponseSpecs.requestReturnsUnauthorized())
                .perform(orderUUID, request);
    }

    @Test
    public void adminCannotMarkFulfillerStatusForNonExistentOrder() {
        FulfillerDetailsRequest request = AdminSteps.fulfillerDetailsRequest(FulfillerStatus.IN_PROGRESS, null);

        new ActionRequester(
                RequestSpecs.adminSpec(),
                Endpoint.ORDER_FULFILLER_DETAILS,
                ResponseSpecs.requestReturnsNotFound())
                .perform(UUID.randomUUID().toString(), request);
    }

    @Test
    @DisplayName("auth required to discontinue a lab test order")
    public void authRequiredToDiscontinueLabOrder() {
        String patientUUID = SessionStorage.getPatient().getUuid();
        String orderUUID = SessionStorage.getOrderUuid();
        String encounterUUID = SessionStorage.getOrderEncounter().getUuid();

        new CrudRequester(
                RequestSpecs.unAuthSpec(),
                Endpoint.ORDER_POST,
                ResponseSpecs.requestReturnsBadRequest())
                .create(AdminSteps.discontinueOrderRequest(orderUUID, patientUUID, encounterUUID));
    }

    @Test
    @DisplayName("admin cannot discontinue an already discontinued lab test order")
    public void adminCannotDiscontinueOrderTwice() {
        String patientUUID = SessionStorage.getPatient().getUuid();
        String orderUUID = SessionStorage.getOrderUuid();
        String encounterUUID = SessionStorage.getOrderEncounter().getUuid();

        AdminSteps.discontinueOrder(orderUUID, patientUUID, encounterUUID);

        DiscontinueOrderRequest repeatedDiscontinueRequest = AdminSteps.discontinueOrderRequest(orderUUID, patientUUID, encounterUUID);
        new CrudRequester(
                RequestSpecs.adminSpec(),
                Endpoint.ORDER_POST,
                ResponseSpecs.requestReturnsServerError())
                .create(repeatedDiscontinueRequest);
    }
}
