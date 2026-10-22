package apiTests.laboratory;

import apiParts.assertions.ModelAssertions;
import apiParts.generators.LabResultGenerator;
import apiParts.generators.RandomModelGenerator;
import apiParts.models.encounter.CreateEncounterRequest;
import apiParts.models.encounter.EncounterResponse;
import apiParts.models.encounter.ObsResponse;
import apiParts.models.order.DiscontinueOrderRequest;
import apiParts.models.order.FulfillerDetailsRequest;
import apiParts.models.order.FulfillerStatus;
import apiParts.models.order.Order;
import apiParts.models.order.OrderAction;
import apiParts.skelethon.endpoints.Endpoint;
import apiParts.skelethon.requests.crud.CrudRequester;
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
        Order receivedOrder = AdminSteps.getOrder(orderUUID);
        String completionComment = RandomModelGenerator.randomSentence();

        // 1) laboratory starts processing the sample
        AdminSteps.markOrderFulfillerStatus(orderUUID, FulfillerStatus.IN_PROGRESS);
        ModelAssertions.assertMatchesExpected(
                AdminSteps.getOrder(orderUUID),
                Order.builder().fulfillerStatus(FulfillerStatus.IN_PROGRESS).build(),
                "lab order after starting the test");

        // 2) laboratory enters the result against the order
        CreateEncounterRequest resultRequest =
                LabResultGenerator.generateAlkalinePhosphataseResult(patientUUID, orderUUID);
        AdminSteps.addResultsToEncounter(encounterUUID, resultRequest);

        EncounterResponse savedResultEncounter = AdminSteps.getEncounter(encounterUUID);
        ModelAssertions.assertThatModels(resultRequest, savedResultEncounter)
                .as("lab result encounter")
                .match();
        softly.assertThat(savedResultEncounter.getObs())
                .as("result obs saved to the encounter")
                .hasSize(1);
        ObsResponse savedObs = AdminSteps.getObs(patientUUID, savedResultEncounter.getObs().get(0).getUuid());
        ModelAssertions.assertThatModels(resultRequest.getObs(), List.of(savedObs))
                .as("saved lab result")
                .match();
        softly.assertThat(savedObs.getPerson().getUuid())
                .as("result belongs to the patient")
                .isEqualTo(patientUUID);

        // 3) the tested order is discontinued once a result is captured
        Order discontinuation = AdminSteps.discontinueOrder(orderUUID, patientUUID, encounterUUID);

        softly.assertThat(discontinuation.getAction())
                .as("discontinuation order action")
                .isEqualTo(OrderAction.DISCONTINUE.name());
        softly.assertThat(discontinuation.getPreviousOrder().getUuid())
                .as("discontinuation references the tested order")
                .isEqualTo(orderUUID);
        softly.assertThat(AdminSteps.getOrder(orderUUID).getDateStopped())
                .as("tested order is stopped after discontinuation")
                .isNotNull();

        // 4) laboratory marks the order as completed with a comment
        AdminSteps.markOrderFulfillerStatus(orderUUID, FulfillerStatus.COMPLETED, completionComment);
        Order completedOrder = AdminSteps.getOrder(orderUUID);
        ModelAssertions.assertMatchesExpected(
                completedOrder,
                Order.builder()
                        .fulfillerStatus(FulfillerStatus.COMPLETED)
                        .fulfillerComment(completionComment)
                        .build(),
                "completed lab order");
        softly.assertThat(completedOrder)
                .as("lab order data changed after processing")
                .usingRecursiveComparison()
                .isNotEqualTo(receivedOrder);
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