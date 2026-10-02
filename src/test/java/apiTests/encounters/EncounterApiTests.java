package apiTests.encounters;

import apiParts.assertions.EncounterAssertions;
import apiParts.assertions.ModelAssertions;
import apiParts.generators.DrugOrderGenerator;
import apiParts.generators.RandomModelGenerator;
import apiParts.models.Location;
import apiParts.models.encounter.EncounterResponse;
import apiParts.models.vitals.CreateVitalsRequest;
import apiParts.steps.AdminSteps;
import apiParts.steps.VisitSteps;
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

        EncounterAssertions.assertCreatedEncounter(
                encounter, request.getObs().size(), EncounterResponse::getObs, "obs");

        EncounterResponse savedEncounter = AdminSteps.getEncounter(encounter.getUuid());
        EncounterAssertions.assertEncounterPersisted(
                encounter, savedEncounter, EncounterResponse::getObs, "vitals observations");
        ModelAssertions.assertThatModels(request, savedEncounter)
                .as("GET /encounter response")
                .match();
    }

    @Test
    public void adminCanCreateDrugOrderEncounter() {
        var order = DrugOrderGenerator.generateDrugOrder();
        var request = DrugOrderGenerator.generateEncounterRequest(order);
        EncounterResponse encounter = AdminSteps.createEncounter(request);

        EncounterAssertions.assertCreatedEncounter(
                encounter, request.getOrders().size(), EncounterResponse::getOrders, "orders");

        EncounterResponse savedEncounter = AdminSteps.getEncounter(encounter.getUuid());
        EncounterAssertions.assertEncounterPersisted(
                encounter, savedEncounter, EncounterResponse::getOrders, "drug order references");
        ModelAssertions.assertThatModels(request, savedEncounter)
                .as("GET /encounter response")
                .match();
    }

    @Test
    public void adminCanAttachTwoDifferentEncounterTypesToSameVisit() {
        var visit = VisitSteps.createVisitWithRequiredFields(patientUUID);

        CreateVitalsRequest vitalsRequest = RandomModelGenerator.generate(CreateVitalsRequest.class);
        vitalsRequest.setLocation(Location.OUTPATIENT_CLINIC);
        vitalsRequest.setVisit(visit.getUuid());
        EncounterResponse vitalsEncounter = AdminSteps.createEncounter(vitalsRequest);

        var drugOrder = DrugOrderGenerator.generateDrugOrder();
        var orderRequest = DrugOrderGenerator.generateEncounterRequest(drugOrder);
        orderRequest.setVisit(visit.getUuid());
        EncounterResponse orderEncounter = AdminSteps.createEncounter(orderRequest);

        EncounterAssertions.assertDifferentEncounterTypesInVisit(
                VisitSteps.getVisit(visit.getUuid()), orderEncounter, vitalsEncounter);
        EncounterResponse savedVitalsEncounter = AdminSteps.getEncounter(vitalsEncounter.getUuid());
        EncounterResponse savedOrderEncounter = AdminSteps.getEncounter(orderEncounter.getUuid());
        ModelAssertions.assertThatModels(vitalsRequest, savedVitalsEncounter)
                .as("saved vitals encounter in visit")
                .match();
        ModelAssertions.assertThatModels(orderRequest, savedOrderEncounter)
                .as("saved drug order encounter in visit")
                .match();
    }
}