package apiTests.medications;

import apiParts.assertions.ModelAssertions;
import apiParts.models.encounter.CreateEncounterResponse;
import apiParts.models.order.Drug;
import apiParts.models.order.DrugOrder;
import apiParts.models.order.DrugOrderResponse;
import apiParts.skelethon.requests.crud.CrudRequester;
import apiParts.skelethon.endpoints.Endpoint;
import apiParts.skelethon.requests.crud.SuccessfulCrudRequester;
import apiParts.specs.RequestSpecs;
import apiParts.specs.ResponseSpecs;
import apiParts.steps.AdminSteps;
import apiParts.utils.DateTimeUtils;
import apiTests.BaseTest;
import common.annotations.CreateOrder;
import common.annotations.CreatePatient;
import common.storages.SessionStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static common.annotations.CreateOrder.Type.DRUG;

@CreatePatient
public class MedicationsApiTests extends BaseTest {

    private String patientUUID;

    @BeforeEach
    void setUp() {
        patientUUID = SessionStorage.getPatient().getUuid();
    }

    @Test
    public void activeMedicationHasNoScheduledDateOrDateStopped() {
        var request = AdminSteps.drugOrderEncounterRequest(patientUUID);
        new SuccessfulCrudRequester<CreateEncounterResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.ENCOUNTER_POST,
                ResponseSpecs.requestReturnsCreated())
                .create(request);

        var medications = AdminSteps.fetchMedications(patientUUID);
        softly.assertThat(medications.results())
                .as("active drug orders saved for patient")
                .hasSize(1);

        var saved = medications.results().get(0);
        ModelAssertions.assertThatModels(softly, request.getOrders(), medications.results())
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
        var request = AdminSteps.upcomingDrugOrderEncounterRequest(patientUUID);
        new SuccessfulCrudRequester<CreateEncounterResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.ENCOUNTER_POST,
                ResponseSpecs.requestReturnsCreated())
                .create(request);

        var medications = AdminSteps.fetchMedications(patientUUID);
        softly.assertThat(medications.results())
                .as("upcoming drug orders saved for patient")
                .hasSize(1);

        var saved = medications.results().get(0);
        ModelAssertions.assertThatModels(softly, request.getOrders(), medications.results())
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
                .isEqualTo(DateTimeUtils.toInstantString(((DrugOrder) request.getOrders().get(0)).getScheduledDate()));
        softly.assertThat(saved.getDateStopped())
                .as("upcoming order is not stopped")
                .isNull();
    }

    @Test
    @CreateOrder(DRUG)
    public void discontinuedMedicationBecomesPastAndReplacesOriginalInList() {
        var originalOrderUUID = SessionStorage.getOrderUuid();

        var discontinued = AdminSteps.discontinueDrugOrderEncounter(patientUUID, originalOrderUUID, Drug.ASPIRIN_325MG);
        softly.assertThat(discontinued.getOrders())
                .as("DISCONTINUE creates an order record")
                .hasSize(1);
        var discontinueStubUUID = discontinued.getOrders().get(0).getUuid();
        softly.assertThat(discontinueStubUUID)
                .as("DISCONTINUE creates a new order record, distinct from the original")
                .isNotEqualTo(originalOrderUUID);

        var medications = AdminSteps.fetchMedications(patientUUID);
        softly.assertThat(medications.results())
                .as("excludeDiscontinueOrders hides the DISCONTINUE stub, original order remains")
                .hasSize(1)
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
        var request = AdminSteps.drugOrderEncounterRequest(patientUUID);
        var before = AdminSteps.fetchMedications(patientUUID).results();

        new CrudRequester(
                RequestSpecs.unAuthSpec(),
                Endpoint.ENCOUNTER_POST,
                ResponseSpecs.requestReturnsBadRequest())
                .create(request);

        ModelAssertions.assertUnchanged(softly, before, AdminSteps.fetchMedications(patientUUID).results(),
                "medications after unauthorized create");
    }

}
