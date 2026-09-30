package apiTests.encounters;

import apiParts.generators.DrugOrderGenerator;
import apiParts.generators.RandomModelGenerator;
import apiParts.models.Location;
import apiParts.models.Ref;
import apiParts.models.encounter.EncounterResponse;
import apiParts.models.encounter.EncounterType;
import apiParts.models.vitals.CreateVitalsRequest;
import apiParts.steps.AdminSteps;
import apiTests.BaseTest;
import common.annotations.CreatePatient;
import common.storages.SessionStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@CreatePatient
public class EncounterApiTests extends BaseTest {

    private String patientUUID;

    @BeforeEach
    void setUp() {
        patientUUID = SessionStorage.getPatient().getUuid();
    }

    @Test
    public void adminCanCreateVitalsEncounter() {
        CreateVitalsRequest request = RandomModelGenerator.generate(CreateVitalsRequest.class);
        request.setLocation(Location.OUTPATIENT_CLINIC);
        EncounterResponse encounter = AdminSteps.createEncounter(request);

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
                .hasSize(request.getObs().size());

        EncounterResponse savedEncounter = AdminSteps.getEncounter(encounter.getUuid());
        softly.assertThat(savedEncounter.getUuid())
                .as("created vitals encounter is retrievable")
                .isEqualTo(encounter.getUuid());
        softly.assertThat(savedEncounter.getPatient().getUuid())
                .as("persisted encounter patient")
                .isEqualTo(patientUUID);
        softly.assertThat(savedEncounter.getEncounterType().getUuid())
                .as("persisted encounter type")
                .isEqualTo(EncounterType.VITALS.getUuid());
        softly.assertThat(savedEncounter.getObs())
                .extracting(Ref::getUuid)
                .as("persisted vitals observations")
                .containsExactlyInAnyOrderElementsOf(
                        encounter.getObs().stream().map(Ref::getUuid).toList());
    }

    @Test
    public void adminCanCreateDrugOrderEncounter() {
        var order = DrugOrderGenerator.generateDrugOrder();
        var request = DrugOrderGenerator.generateEncounterRequest(order);
        EncounterResponse encounter = AdminSteps.createEncounter(request);

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
                .hasSize(request.getOrders().size());

        EncounterResponse savedEncounter = AdminSteps.getEncounter(encounter.getUuid());
        softly.assertThat(savedEncounter.getPatient().getUuid())
                .as("persisted encounter patient")
                .isEqualTo(patientUUID);
        softly.assertThat(savedEncounter.getEncounterType().getUuid())
                .as("persisted encounter type")
                .isEqualTo(EncounterType.ORDER.getUuid());
        softly.assertThat(savedEncounter.getOrders())
                .extracting(Ref::getUuid)
                .as("persisted drug order references")
                .containsExactlyInAnyOrderElementsOf(
                        encounter.getOrders().stream().map(Ref::getUuid).toList());
    }

    // Creates a Vitals encounter and a Drug Order encounter for the same patient
    // and attaches both to one visit - checks that different encounter types coexist correctly.
    //
    // OpenMRS links an encounter to a visit via the encounter's own "visit" field
    // (Encounter owns the foreign key); a visit's "encounters" list on POST /visit is not
    // a persisted association - it's only echoed back in that response and is empty again
    // on a subsequent GET. So the visit is created first, then each encounter is created with
    // "visit" set to the new visit's uuid.
    @Test
    public void adminCanAttachTwoDifferentEncounterTypesToSameVisit() {
        var visit = AdminSteps.createVisitWithRequiredFields(patientUUID);

        CreateVitalsRequest vitalsRequest = RandomModelGenerator.generate(CreateVitalsRequest.class);
        vitalsRequest.setLocation(Location.OUTPATIENT_CLINIC);
        vitalsRequest.setVisit(visit.getUuid());
        EncounterResponse vitalsEncounter = AdminSteps.createEncounter(vitalsRequest);

        var drugOrder = DrugOrderGenerator.generateDrugOrder();
        var orderRequest = DrugOrderGenerator.generateEncounterRequest(drugOrder);
        orderRequest.setVisit(visit.getUuid());
        EncounterResponse orderEncounter = AdminSteps.createEncounter(orderRequest);

        softly.assertThat(vitalsEncounter.getEncounterType().getUuid())
                .as("vitals encounter type")
                .isEqualTo(EncounterType.VITALS.getUuid());
        softly.assertThat(orderEncounter.getEncounterType().getUuid())
                .as("order encounter type")
                .isEqualTo(EncounterType.ORDER.getUuid());
        softly.assertThat(orderEncounter.getEncounterType().getUuid())
                .as("encounter types are different")
                .isNotEqualTo(vitalsEncounter.getEncounterType().getUuid());

        softly.assertThat(AdminSteps.getVisit(visit.getUuid()).getEncounters())
                .extracting(Ref::getUuid)
                .as("created visit is retrievable with both encounters")
                .containsExactlyInAnyOrder(vitalsEncounter.getUuid(), orderEncounter.getUuid());
    }

}
