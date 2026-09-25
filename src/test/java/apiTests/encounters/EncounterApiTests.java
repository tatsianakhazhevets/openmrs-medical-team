package apiTests.encounters;

import apiParts.models.EncounterType;
import apiParts.models.encounter.CreateEncounterResponse;
import apiParts.models.encounter.Ref;
import apiParts.models.visit.CreateVisitRequest;
import apiParts.models.visit.CreateVisitResponse;
import apiParts.skelethon.endpoints.Endpoint;
import apiParts.skelethon.requests.crud.SuccessfulCrudRequester;
import apiParts.specs.RequestSpecs;
import apiParts.specs.ResponseSpecs;
import apiParts.steps.AdminSteps;
import apiTests.BaseTest;
import common.annotations.CreatePatient;
import common.storages.SessionStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


@CreatePatient
public class EncounterApiTests extends BaseTest {
    private static final int VITALS_OBS_COUNT = 11; // obs in AdminSteps.createVitalsEncounter()

    private String patientUUID;

    @BeforeEach
    void setUp() {
        patientUUID = SessionStorage.getPatient().getUuid();
    }

    @Test
    public void adminCanCreateVitalsEncounter() {
        CreateEncounterResponse encounter = AdminSteps.createVitalsEncounter(patientUUID);

        softly.assertThat(encounter.getUuid())
                .as("encounter uuid")
                .isNotBlank();
        softly.assertThat(encounter.getEncounterType().getUuid())
                .as("encounter type uuid")
                .isEqualTo(EncounterType.VITALS.getUuid());
        softly.assertThat(encounter.getPatient().getUuid())
                .as("patient uuid")
                .isEqualTo(patientUUID);
        softly.assertThat(encounter.getObs())
                .as("obs saved with encounter")
                .hasSize(VITALS_OBS_COUNT);
    }

    @Test
    public void adminCanCreateDrugOrderEncounter() {
        CreateEncounterResponse encounter = AdminSteps.createDrugOrderEncounter(patientUUID);

        softly.assertThat(encounter.getUuid())
                .as("encounter uuid")
                .isNotBlank();
        softly.assertThat(encounter.getEncounterType().getUuid())
                .as("encounter type uuid")
                .isEqualTo(EncounterType.ORDER.getUuid());
        softly.assertThat(encounter.getPatient().getUuid())
                .as("patient uuid")
                .isEqualTo(patientUUID);
        softly.assertThat(encounter.getOrders())
                .as("orders saved with encounter")
                .hasSize(1);
    }

    // Creates a Vitals encounter and a Drug Order encounter for the same patient
    // and attaches both to one visit - checks that different encounter types coexist correctly.
    @Test
    public void adminCanAttachTwoDifferentEncounterTypesToSameVisit() {
        CreateEncounterResponse vitalsEncounter = AdminSteps.createVitalsEncounter(patientUUID);
        CreateEncounterResponse orderEncounter = AdminSteps.createDrugOrderEncounter(patientUUID);

        softly.assertThat(vitalsEncounter.getEncounterType().getUuid())
                .as("vitals encounter type")
                .isEqualTo(EncounterType.VITALS.getUuid());
        softly.assertThat(orderEncounter.getEncounterType().getUuid())
                .as("order encounter type")
                .isEqualTo(EncounterType.ORDER.getUuid());
        softly.assertThat(orderEncounter.getEncounterType().getUuid())
                .as("encounter types are different")
                .isNotEqualTo(vitalsEncounter.getEncounterType().getUuid());

        CreateVisitRequest visitRequest = AdminSteps.visitRequest(
                patientUUID,
                java.util.List.of(vitalsEncounter.getUuid(), orderEncounter.getUuid()));

        var visit = new SuccessfulCrudRequester<CreateVisitResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.VISIT_POST,
                ResponseSpecs.requestReturnsCreated())
                .create(visitRequest);

        softly.assertThat(visit.getEncounters())
                .extracting(Ref::getUuid)
                .as("visit contains both encounters")
                .containsExactlyInAnyOrder(vitalsEncounter.getUuid(), orderEncounter.getUuid());
    }
}
