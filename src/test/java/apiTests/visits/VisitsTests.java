package apiTests.visits;

import apiParts.assertions.ModelAssertions;
import apiParts.generators.RandomModelGenerator;
import apiParts.models.encounter.EncounterType;
import apiParts.models.visit.*;
import apiParts.models.Ref;
import apiParts.skelethon.endpoints.Endpoint;
import apiParts.skelethon.requests.crud.CrudRequester;
import apiParts.skelethon.requests.crud.SuccessfulCrudRequester;
import apiParts.specs.RequestSpecs;
import apiParts.specs.ResponseSpecs;
import apiParts.steps.AdminSteps;
import apiTests.BaseTest;
import common.annotations.CreateEncounter;
import common.annotations.CreatePatient;
import common.storages.SessionStorage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

@CreatePatient
public class VisitsTests extends BaseTest {
    private static final String NON_EXISTENT_VISIT_UUID =
            "82f18b44-6814-11e8-923f-e9a88dcb533f";
    private String patientUUID;
    private String visitUUID;

    @BeforeEach
    public void setUp() {
        patientUUID = SessionStorage.getPatient().getUuid();
    }

    @AfterEach
    public void tearDown() {
        if (visitUUID != null) {
            AdminSteps.deleteVisit(visitUUID);
        }
    }


    @Test
    public void adminCanCreateVisitOnlyWithRequiredFields() {
        CreateVisitRequest request =
                RandomModelGenerator.generate(CreateVisitRequest.class);
        request.setLocation(null);
        request.setStartDatetime(null);
        request.setStopDatetime(null);
        request.setEncounters(null);
        request.setAttributes(null);

        CreateVisitResponse visit = AdminSteps.createVisit(request);
        visitUUID = visit.getUuid();
        GetVisitResponse savedVisit = AdminSteps.getVisit(visitUUID);

        softly.assertThat(savedVisit.getUuid()).as("visit uuid").isEqualTo(visit.getUuid());
        ModelAssertions.assertThatModels(request, savedVisit).as("created visit").match();
        softly.assertThat(savedVisit.getLocation()).as("location").isNull();
        softly.assertThat(savedVisit.getStopDatetime()).as("stop datetime").isNull();
        softly.assertThat(savedVisit.getStartDatetime()).as("start datetime").isNotNull();
        softly.assertThat(savedVisit.getEncounters()).as("encounters").isEmpty();
        softly.assertThat(savedVisit.getAttributes()).as("attributes").isEmpty();
    }

    @CreateEncounter(EncounterType.VITALS)
    @Test
    public void adminCanCreateVisitWithOptionalFields() {
        String encounterUUID = SessionStorage.getEncounter().getUuid();

        CreateVisitRequest request =
                RandomModelGenerator.generate(CreateVisitRequest.class);
        request.setEncounters(List.of(encounterUUID));

        CreateVisitResponse visit = AdminSteps.createVisit(request);
        visitUUID = visit.getUuid();

        GetVisitResponse savedVisit = AdminSteps.getVisit(visitUUID);
        Attribute attribute = request.getAttributes().get(0);

        softly.assertThat(savedVisit.getUuid()).as("visit uuid").isEqualTo(visit.getUuid());
        ModelAssertions.assertThatModels(request, savedVisit).as("created visit").match();
        softly.assertThat(savedVisit.getAttributes()).extracting(Ref::getDisplay)
                .containsExactly(attribute.getAttributeType().getDisplay()
                        + ": " + attribute.getValue());
        softly.assertThat(savedVisit.getStopDatetime()).as("stop datetime").isNotNull();
        softly.assertThat(savedVisit.getStartDatetime()).as("start datetime").isNotNull();
    }

    @Test
    public void unauthorizedUserCannotCreateVisit() {
        CreateVisitRequest request =
                RandomModelGenerator.generate(CreateVisitRequest.class);

        List<String> visitUUIDsBefore = AdminSteps.getVisits(request.getPatient())
                .results()
                .stream()
                .map(GetVisitResponse::getUuid)
                .toList();

        new CrudRequester(
                RequestSpecs.unAuthSpec(),
                Endpoint.VISIT_POST,
                ResponseSpecs.requestReturnsBadRequestWithMessage("Privileges required: Get Patients"))
                .create(request);

        List<String> visitUUIDsAfter = AdminSteps.getVisits(request.getPatient())
                .results()
                .stream()
                .map(GetVisitResponse::getUuid)
                .toList();

        softly.assertThat(visitUUIDsAfter).as("visits should not change after unauthorized create")
                .containsExactlyInAnyOrderElementsOf(visitUUIDsBefore);
    }

    @Test
    public void cannotCreateVisitWithoutPatient() {
        List<String> visitUUIDsBefore = AdminSteps.getVisits(patientUUID)
                .results()
                .stream()
                .map(GetVisitResponse::getUuid)
                .toList();

        CreateVisitRequest request =
                RandomModelGenerator.generate(CreateVisitRequest.class);
        request.setPatient(null);

        new CrudRequester(
                RequestSpecs.adminSpec(),
                Endpoint.VISIT_POST,
                ResponseSpecs.requestReturnsBadRequestWithMessage(
                        "Some required properties are missing: patient"))
                .create(request);

        List<String> visitUUIDsAfter = AdminSteps.getVisits(patientUUID)
                .results()
                .stream()
                .map(GetVisitResponse::getUuid)
                .toList();

        softly.assertThat(visitUUIDsAfter).as("visits should not change after create without patient")
                .containsExactlyInAnyOrderElementsOf(visitUUIDsBefore);
    }

    @CreateEncounter(EncounterType.VITALS)
    @Test
    public void adminCanUpdateVisit() {
        String encounterUUID = SessionStorage.getEncounter().getUuid();

        CreateVisitRequest request =
                RandomModelGenerator.generate(CreateVisitRequest.class);
        request.setEncounters(List.of(encounterUUID));

        CreateVisitResponse visit = AdminSteps.createVisit(request);
        visitUUID = visit.getUuid();

        UpdateVisitRequest updatedRequest =
                RandomModelGenerator.generate(UpdateVisitRequest.class);
        updatedRequest.setVisitType(VisitType.HOME_VISIT);

        new SuccessfulCrudRequester<CreateVisitResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.VISIT_POST,
                ResponseSpecs.requestReturnsOk()
        ).update(visit.getUuid(), updatedRequest);

        GetVisitResponse updatedVisit = AdminSteps.getVisit(visitUUID);
        softly.assertThat(updatedVisit.getUuid()).as("visit uuid").isEqualTo(visit.getUuid());
        softly.assertThat(updatedVisit.getPatient().getUuid()).as("patient uuid").isEqualTo(patientUUID);
        softly.assertThat(updatedVisit.getVisitType().getUuid()).as("visit type")
                .isEqualTo(updatedRequest.getVisitType().getUuid());
    }

    @Test
    public void updateNonExistentVisitReturnsNotFound() {
        UpdateVisitRequest updatedRequest =
                RandomModelGenerator.generate(UpdateVisitRequest.class);

        new CrudRequester(
                RequestSpecs.adminSpec(),
                Endpoint.VISIT_POST,
                ResponseSpecs.requestReturnsNotFound())
                .update(NON_EXISTENT_VISIT_UUID, updatedRequest);

        new CrudRequester(
                RequestSpecs.adminSpec(),
                Endpoint.VISIT_GET,
                ResponseSpecs.requestReturnsNotFound())
                .get(NON_EXISTENT_VISIT_UUID);
    }

    @Test
    public void unauthorizedUserCannotUpdateVisit() {
        CreateVisitRequest request =
                RandomModelGenerator.generate(CreateVisitRequest.class);

        CreateVisitResponse visit = AdminSteps.createVisit(request);
        visitUUID = visit.getUuid();

        GetVisitResponse savedVisit = AdminSteps.getVisit(visitUUID);

        UpdateVisitRequest updatedRequest =
                RandomModelGenerator.generate(UpdateVisitRequest.class);

        new CrudRequester(
                RequestSpecs.unAuthSpec(),
                Endpoint.VISIT_POST,
                ResponseSpecs.requestReturnsUnauthorized())
                .update(visit.getUuid(), updatedRequest);

        GetVisitResponse actualVisit = AdminSteps.getVisit(visitUUID);
        softly.assertThat(actualVisit.getUuid()).as("visit uuid").isEqualTo(savedVisit.getUuid());
        softly.assertThat(actualVisit.getPatient().getUuid()).as("patient uuid")
                .isEqualTo(savedVisit.getPatient().getUuid());
        softly.assertThat(actualVisit.getVisitType().getUuid()).as("visit type")
                .isEqualTo(savedVisit.getVisitType().getUuid());
    }

    @CreateEncounter(EncounterType.VITALS)
    @Test
    public void adminCanRetireVisit() {
        String encounterUUID = SessionStorage.getEncounter().getUuid();
        CreateVisitRequest request =
                RandomModelGenerator.generate(CreateVisitRequest.class);
        request.setEncounters(List.of(encounterUUID));
        CreateVisitResponse visit = AdminSteps.createVisit(request);

        new SuccessfulCrudRequester<CreateVisitResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.VISIT_DELETE,
                ResponseSpecs.requestReturnsNoContent()).delete(visit.getUuid());

        GetVisitResponse retiredVisit = AdminSteps.getVisit(visit.getUuid());

        softly.assertThat(retiredVisit.getUuid()).as("visit uuid").isEqualTo(visit.getUuid());
        softly.assertThat(retiredVisit.getVoided()).as("visit should be retired").isTrue();
    }

    @CreateEncounter(EncounterType.VITALS)
    @Test
    public void adminCanPurgeVisit() {
        String encounterUUID = SessionStorage.getEncounter().getUuid();
        CreateVisitRequest request =
                RandomModelGenerator.generate(CreateVisitRequest.class);
        request.setEncounters(List.of(encounterUUID));

        CreateVisitResponse visit = AdminSteps.createVisit(request);

        new SuccessfulCrudRequester<CreateVisitResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.VISIT_DELETE,
                ResponseSpecs.requestReturnsNoContent()).delete(visit.getUuid(), Map.of("purge", true));
        new CrudRequester(
                RequestSpecs.adminSpec(),
                Endpoint.VISIT_GET,
                ResponseSpecs.requestReturnsNotFound())
                .get(visit.getUuid());
    }

    @Test
    public void deleteNonExistentVisitReturnsNotFound() {
        new CrudRequester(
                RequestSpecs.adminSpec(),
                Endpoint.VISIT_DELETE,
                ResponseSpecs.requestReturnsNotFound())
                .delete(NON_EXISTENT_VISIT_UUID);

        new CrudRequester(
                RequestSpecs.adminSpec(),
                Endpoint.VISIT_GET,
                ResponseSpecs.requestReturnsNotFound())
                .get(NON_EXISTENT_VISIT_UUID);
    }

    @Test
    public void unauthorizedUserCannotDeleteVisit() {
        CreateVisitRequest request =
                RandomModelGenerator.generate(CreateVisitRequest.class);

        CreateVisitResponse visit = AdminSteps.createVisit(request);
        visitUUID = visit.getUuid();

        GetVisitResponse savedVisit = AdminSteps.getVisit(visitUUID);

        new CrudRequester(
                RequestSpecs.unAuthSpec(),
                Endpoint.VISIT_DELETE,
                ResponseSpecs.requestReturnsUnauthorized())
                .delete(visitUUID);

        GetVisitResponse actualVisit = AdminSteps.getVisit(visitUUID);

        softly.assertThat(actualVisit.getUuid()).as("visit uuid")
                .isEqualTo(savedVisit.getUuid());
        softly.assertThat(actualVisit.getPatient().getUuid()).as("patient uuid")
                .isEqualTo(savedVisit.getPatient().getUuid());
        softly.assertThat(actualVisit.getVisitType().getUuid()).as("visit type")
                .isEqualTo(savedVisit.getVisitType().getUuid());
        softly.assertThat(actualVisit.getVoided()).as("visit should not be retired")
                .isEqualTo(savedVisit.getVoided());
    }
}
