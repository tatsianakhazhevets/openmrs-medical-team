package apiTests.medications;

import apiParts.assertions.ModelAssertions;
import apiParts.assertions.OrderAssertions;
import apiParts.generators.DrugOrderGenerator;
import apiParts.models.encounter.CreateEncounterRequest;
import apiParts.models.order.DrugOrder;
import apiParts.skelethon.endpoints.Endpoint;
import apiParts.skelethon.requests.crud.CrudRequester;
import apiParts.specs.RequestSpecs;
import apiParts.specs.ResponseSpecs;
import apiParts.steps.AdminSteps;
import apiTests.BaseTest;
import common.annotations.CreatePatient;
import common.storages.SessionStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
        OrderAssertions.assertPersistedOrderReferences(request, encounter, savedEncounter,
                "persisted order references");

        var medications = AdminSteps.fetchMedications(patientUUID);
        OrderAssertions.assertActiveOrders(request.getOrders(), medications.results());
        ModelAssertions.assertThatModels(request.getOrders(), medications.results())
                .as("active drug order fields")
                .match();
    }

    @Test
    public void upcomingMedicationHasFutureScheduledDateAndNoDateStopped() {
        DrugOrder upcomingDrugOrder = DrugOrderGenerator.generateUpcomingDrugOrder();
        var upcomingRequest = DrugOrderGenerator.generateEncounterRequest(upcomingDrugOrder);
        var encounter = AdminSteps.createEncounter(upcomingRequest);
        var savedEncounter = AdminSteps.getEncounter(encounter.getUuid());
        OrderAssertions.assertPersistedOrderReferences(upcomingRequest, encounter, savedEncounter,
                "persisted order references");

        var medications = AdminSteps.fetchMedications(patientUUID);
        OrderAssertions.assertUpcomingOrder(upcomingDrugOrder, medications.results());
        ModelAssertions.assertThatModels(upcomingRequest.getOrders(), medications.results())
                .as("upcoming drug order fields")
                .match();
    }

    @Test
    public void discontinuedMedicationBecomesPastAndReplacesOriginalInList() {
        var originalEncounter = AdminSteps.createEncounter(request);

        var savedOriginalEncounter = AdminSteps.getEncounter(originalEncounter.getUuid());
        OrderAssertions.assertPersistedOrderReferences(request, originalEncounter, savedOriginalEncounter,
                "persisted original order references");
        var originalOrderUUID = originalEncounter.getOrders().get(0).getUuid();

        var discontinueRequest = DrugOrderGenerator.generateDiscontinueEncounterRequest(
                originalOrderUUID, drugOrder.getDrug());

        var discontinued = AdminSteps.createEncounter(discontinueRequest);
        var savedDiscontinueEncounter = AdminSteps.getEncounter(discontinued.getUuid());
        OrderAssertions.assertPersistedOrderReferences(discontinueRequest, discontinued, savedDiscontinueEncounter,
                "DISCONTINUE order persisted");

        var medications = AdminSteps.fetchMedications(patientUUID);
        OrderAssertions.assertDiscontinuedOrderIsPast(discontinueRequest, discontinued,
                originalOrderUUID, medications.results());
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