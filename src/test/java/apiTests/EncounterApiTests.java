package apiTests;

import apiParts.assertions.ModelAssertions;
import apiParts.generators.RandomModelGenerator;
import apiParts.generators.RandomUuidGenerator;
import apiParts.models.GetParams;
import apiParts.models.euncouterTest.EncounterTestRequest;
import apiParts.models.euncouterTest.EncounterTestResponse;
import apiParts.models.euncouterTest.GetEncounterResponse;
import apiParts.skelethon.endpoints.Endpoint;
import apiParts.skelethon.requests.crud.CrudRequester;
import apiParts.skelethon.requests.crud.SuccessfulCrudRequester;
import apiParts.specs.RequestSpecs;
import apiParts.specs.ResponseSpecs;
import common.annotations.CreatePatient;
import common.storages.SessionStorage;
import org.junit.jupiter.api.Test;

import static apiParts.models.errors.EncounterErrorMessages.*;

@CreatePatient
public class EncounterApiTests extends BaseTest {

    String nonExistingUuid = RandomUuidGenerator.generateUuid();

    @Test
    @CreatePatient
    public void adminCanCreateEncounter() {

        EncounterTestRequest encounterRequest = RandomModelGenerator.generate(EncounterTestRequest.class);

        EncounterTestResponse encounterResponse = new SuccessfulCrudRequester<EncounterTestResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.ENCOUNTER_CREATE_POST,
                ResponseSpecs.requestReturnsCreated())
                .create(encounterRequest);

        GetEncounterResponse receivedEncounterResponse =
                new SuccessfulCrudRequester<GetEncounterResponse>(
                        RequestSpecs.adminSpec(),
                        Endpoint.ENCOUNTER_RETRIEVE,
                        ResponseSpecs.requestReturnsOk())
                        .get(encounterResponse.getUuid(),
                                GetParams.builder()
                                        .v(GetParams.FULL)
                                        .build()
                                        .toQueryParams());

        ModelAssertions.assertThatModels(softly, encounterRequest, receivedEncounterResponse).match();

        softly.assertThat(receivedEncounterResponse.isVoided()).isFalse();
        softly.assertThat(receivedEncounterResponse.getPatient().getDisplay())
                .isEqualTo(SessionStorage.getPatient().getDisplay());
    }

    @Test
    @CreatePatient
    public void adminCannotCreateEncounterWithInvalidPatient() {
        EncounterTestRequest encounterRequest = RandomModelGenerator.generate(EncounterTestRequest.class);
        encounterRequest.setPatient(null);

        new CrudRequester(
                RequestSpecs.adminSpec(),
                Endpoint.ENCOUNTER_CREATE_POST,
                ResponseSpecs.requestReturnsBadRequestWithTwoMessages(
                        INVALID_SUBMISSION.getMessage(),
                        PATIENT_IS_REQUIRES.getMessage()))
                .create(encounterRequest);

        encounterRequest.setPatient(nonExistingUuid);

        new CrudRequester(
                RequestSpecs.adminSpec(),
                Endpoint.ENCOUNTER_CREATE_POST,
                ResponseSpecs.requestReturnsBadRequestWithTwoMessages(
                        INVALID_SUBMISSION.getMessage(),
                        PATIENT_IS_REQUIRES.getMessage()))
                .create(encounterRequest);
    }

    @Test
    @CreatePatient
    public void adminCannotCreateEncounterWithoutEncounterType() {
        EncounterTestRequest encounterRequest = RandomModelGenerator.generate(EncounterTestRequest.class);
        encounterRequest.setEncounterType(null);

        new CrudRequester(
                RequestSpecs.adminSpec(),
                Endpoint.ENCOUNTER_CREATE_POST,
                ResponseSpecs.requestReturnsBadRequestWithTwoMessages(
                        INVALID_SUBMISSION.getMessage(),
                        ENCOUNTER_TYPE_IS_REQUIRED.getMessage()))
                .create(encounterRequest);
    }

    @Test
    @CreatePatient
    public void adminCannotCreateEncounterWithFutureDatetime() {
        EncounterTestRequest encounterRequest = RandomModelGenerator.generate(EncounterTestRequest.class);
        encounterRequest.setEncounterDatetime(RandomModelGenerator.futureDateTime());

        new CrudRequester(
                RequestSpecs.adminSpec(),
                Endpoint.ENCOUNTER_CREATE_POST,
                ResponseSpecs.requestReturnsBadRequestWithTwoMessages(
                        INVALID_SUBMISSION.getMessage(),
                        ENCOUNTER_DATETIME_SHOULD_BE_BEFORE_THE_CURRENT_DATA.getMessage()))
                .create(encounterRequest);
    }

    @Test
    @CreatePatient
    public void adminCannotGetNonExistingEncounter() {

        new CrudRequester(
                RequestSpecs.adminSpec(),
                Endpoint.ENCOUNTER_RETRIEVE,
                ResponseSpecs.requestReturnsNotFound(
                        OBJECT_WITH_UUID_DOES_NOT_EXIST.getMessage()))
                .get(nonExistingUuid,
                        GetParams.builder()
                                .v(GetParams.FULL)
                                .build()
                                .toQueryParams());
    }

    @Test
    @CreatePatient
    public void adminCanUpdateEncounter() {
        EncounterTestRequest encounterRequest = RandomModelGenerator.generate(EncounterTestRequest.class);

        EncounterTestResponse encounterResponse = new SuccessfulCrudRequester<EncounterTestResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.ENCOUNTER_CREATE_POST,
                ResponseSpecs.requestReturnsCreated())
                .create(encounterRequest);

        encounterRequest.setEncounterDatetime(RandomModelGenerator.pastDateTime());

        EncounterTestResponse updatedEncounterResponse = new SuccessfulCrudRequester<EncounterTestResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.ENCOUNTER_CREATE_POST,
                ResponseSpecs.requestReturnsOk())
                .update(encounterResponse.getUuid(), encounterRequest);

        GetEncounterResponse receivedEncounterResponse =
                new SuccessfulCrudRequester<GetEncounterResponse>(
                        RequestSpecs.adminSpec(),
                        Endpoint.ENCOUNTER_RETRIEVE,
                        ResponseSpecs.requestReturnsOk())
                        .get(encounterResponse.getUuid(),
                                GetParams.builder()
                                        .v(GetParams.FULL)
                                        .build()
                                        .toQueryParams());

        ModelAssertions.assertThatModels(softly, encounterRequest, receivedEncounterResponse).match();
        softly.assertThat(updatedEncounterResponse.getUuid()).isEqualTo(encounterResponse.getUuid());
        softly.assertThat(updatedEncounterResponse.isVoided()).isFalse();
    }

    @Test
    @CreatePatient
    public void adminCannotUpdateNonExistingEncounter() {
        EncounterTestRequest updateRequest = RandomModelGenerator.generate(EncounterTestRequest.class);
        updateRequest.setEncounterDatetime(RandomModelGenerator.pastDateTime());

        new CrudRequester(
                RequestSpecs.adminSpec(),
                Endpoint.ENCOUNTER_CREATE_POST,
                ResponseSpecs.requestReturnsNotFound(
                        OBJECT_WITH_UUID_DOES_NOT_EXIST.getMessage()))
                .update(nonExistingUuid, updateRequest);
    }

    @Test
    @CreatePatient
    public void adminCanDeleteEncounter() {
        EncounterTestRequest encounterRequest = RandomModelGenerator.generate(EncounterTestRequest.class);

        EncounterTestResponse encounterResponse = new SuccessfulCrudRequester<EncounterTestResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.ENCOUNTER_CREATE_POST,
                ResponseSpecs.requestReturnsCreated())
                .create(encounterRequest);

        new SuccessfulCrudRequester<EncounterTestResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.ENCOUNTER_DELETE,
                ResponseSpecs.requestReturnsNoContent())
                .delete(encounterResponse.getUuid());
    }

    @Test
    @CreatePatient
    public void adminCannotDeleteNonExistingEncounter() {
        new CrudRequester(
                RequestSpecs.adminSpec(),
                Endpoint.ENCOUNTER_DELETE,
                ResponseSpecs.requestReturnsNotFound(
                        OBJECT_WITH_UUID_DOES_NOT_EXIST.getMessage()))
                .delete(nonExistingUuid);
    }
}