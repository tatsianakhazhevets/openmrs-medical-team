package apiTests.medications;

import apiParts.assertions.ModelAssertions;
import apiParts.generators.DrugOrderGenerator;
import apiParts.models.Ref;
import apiParts.models.encounter.CreateEncounterRequest;
import apiParts.models.encounter.EncounterResponse;
import apiParts.models.order.DrugOrder;
import apiParts.models.order.DrugOrderResponse;
import apiParts.skelethon.endpoints.Endpoint;
import apiParts.skelethon.requests.crud.CrudRequester;
import apiParts.specs.RequestSpecs;
import apiParts.specs.ResponseSpecs;
import apiParts.steps.AdminSteps;
import apiParts.utils.DateTimeUtils;
import apiTests.BaseTest;
import common.annotations.CreatePatient;
import common.storages.SessionStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

@CreatePatient
public class MedicationsApiTests extends BaseTest {

    private String patientUUID;
    private DrugOrder drugOrder;
    private CreateEncounterRequest request;

    @BeforeEach
    void setUp() {
        patientUUID = SessionStorage.getPatient().getUuid();
        drugOrder = DrugOrderGenerator.generateDrugOrder();
        request = DrugOrderGenerator.generateEncounterRequest(drugOrder);
    }

    @Test
    public void activeMedicationHasNoScheduledDateOrDateStopped() {
        var encounter = AdminSteps.createEncounter(request);
       var savedEncounter = AdminSteps.getEncounter(encounter.getUuid());
        softly.assertThat(savedEncounter.getOrders())
                .extracting(Ref::getUuid)
                .as("persisted order references")
                .hasSize(request.getOrders().size())
                .containsExactlyInAnyOrderElementsOf(
                        encounter.getOrders().stream().map(Ref::getUuid).toList());

        var medications = AdminSteps.fetchMedications(patientUUID);
        softly.assertThat(medications.results())
                .as("active drug orders saved for patient")
                .hasSize(request.getOrders().size());

        var saved = medications.results().get(0);
        ModelAssertions.assertThatModels(request.getOrders(), medications.results())
                .as("active drug order fields")
                .match();
        softly.assertThat(saved.getUrgency())
                .as("active order defaults to ROUTINE urgency")
                .isEqualTo(DrugOrder.URGENCY_ROUTINE);
        softly.assertThat(saved.getScheduledDate())
                .as("active order has no scheduledDate")
                .isNull();
        softly.assertThat(saved.getDateStopped())
                .as("active order is not stopped")
                .isNull();
    }

    @Test
    public void upcomingMedicationHasFutureScheduledDateAndNoDateStopped() {
        DrugOrder upcomingDrugOrder = DrugOrderGenerator.generateUpcomingDrugOrder();
        var upcomingRequest = DrugOrderGenerator.generateEncounterRequest(upcomingDrugOrder);
        var encounter = AdminSteps.createEncounter(upcomingRequest);
        var savedEncounter = AdminSteps.getEncounter(encounter.getUuid());
        softly.assertThat(savedEncounter.getOrders())
                .extracting(Ref::getUuid)
                .as("persisted order references")
                .hasSize(upcomingRequest.getOrders().size())
                .containsExactlyInAnyOrderElementsOf(
                        encounter.getOrders().stream().map(Ref::getUuid).toList());

        var medications = AdminSteps.fetchMedications(patientUUID);
        softly.assertThat(medications.results())
                .as("upcoming drug orders saved for patient")
                .hasSize(upcomingRequest.getOrders().size());

        var saved = medications.results().get(0);
        ModelAssertions.assertThatModels(upcomingRequest.getOrders(), medications.results())
                .as("upcoming drug order fields")
                .match();
        softly.assertThat(saved.getUrgency())
                .as("upcoming order urgency")
                .isEqualTo(DrugOrder.URGENCY_ON_SCHEDULED_DATE);
        softly.assertThat(saved.getScheduledDate())
                .as("upcoming order scheduledDate is in the future")
                .isNotNull()
                .matches(date -> Instant.parse(date).isAfter(Instant.now()), "is after now");
        softly.assertThat(saved.getScheduledDate())
                .as("upcoming order scheduledDate matches the generated request")
                .isEqualTo(DateTimeUtils.toInstantString(upcomingDrugOrder.getScheduledDate()));
        softly.assertThat(saved.getDateStopped())
                .as("upcoming order is not stopped")
                .isNull();
    }

    @Test
    public void discontinuedMedicationBecomesPastAndReplacesOriginalInList() {
        var originalEncounter = AdminSteps.createEncounter(request);

        var savedOriginalEncounter = AdminSteps.getEncounter(originalEncounter.getUuid());
        softly.assertThat(savedOriginalEncounter.getOrders())
                .extracting(Ref::getUuid)
                .as("persisted original order references")
                .hasSize(request.getOrders().size())
                .containsExactlyInAnyOrderElementsOf(
                        originalEncounter.getOrders().stream().map(Ref::getUuid).toList());
        var originalOrderUUID = originalEncounter.getOrders().get(0).getUuid();


        var discontinueRequest = DrugOrderGenerator.generateDiscontinueEncounterRequest(
                originalOrderUUID, drugOrder.getDrug());

        var discontinued = AdminSteps.createEncounter(discontinueRequest);
        var savedDiscontinueEncounter = AdminSteps.getEncounter(discontinued.getUuid());
        softly.assertThat(savedDiscontinueEncounter.getOrders())
                .extracting(Ref::getUuid)
                .as("DISCONTINUE order persisted")
                .containsExactlyInAnyOrderElementsOf(
                        discontinued.getOrders().stream().map(Ref::getUuid).toList());
        softly.assertThat(discontinued.getOrders())
                .as("DISCONTINUE creates an order record")
                .hasSize(discontinueRequest.getOrders().size());
        var discontinueStubUUID = discontinued.getOrders().get(0).getUuid();
        softly.assertThat(discontinueStubUUID)
                .as("DISCONTINUE creates a new order record, distinct from the original")
                .isNotEqualTo(originalOrderUUID);

        var medications = AdminSteps.fetchMedications(patientUUID);
        softly.assertThat(medications.results())
                .as("excludeDiscontinueOrders hides the DISCONTINUE stub, original order remains")
                .hasSize(originalEncounter.getOrders().size())
                .first()
                .extracting(DrugOrderResponse::getUuid)
                .isEqualTo(originalOrderUUID);

        var saved = medications.results().get(0);
        softly.assertThat(saved.getDateStopped())
                .as("original order is stopped once discontinued")
                .isNotNull();
    }

    @Test
    public void unauthorizedUserCannotCreateMedication() {
        var before = AdminSteps.fetchMedications(patientUUID).results();

        new CrudRequester(
                RequestSpecs.unAuthSpec(),
                Endpoint.ENCOUNTER_POST,
                ResponseSpecs.requestReturnsBadRequest())
                .create(request);

        ModelAssertions.assertUnchanged(before, AdminSteps.fetchMedications(patientUUID).results(),
                "medications after unauthorized create");
    }
}