package apiTests;

import apiParts.assertions.ModelAssertions;
import apiParts.generators.RandomModelGenerator;
import apiParts.generators.RandomUuidGenerator;
import apiParts.models.GetParams;
import apiParts.models.encounter.CreateEncounterRequest;
import apiParts.models.encounter.EncounterResponse;
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

        CreateEncounterRequest encounterRequest = RandomModelGenerator.generate(CreateEncounterRequest.class);

        EncounterResponse encounterResponse = new SuccessfulCrudRequester<EncounterResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.ENCOUNTER_POST,
                ResponseSpecs.requestReturnsCreated())
                .create(encounterRequest);

        EncounterResponse receivedEncounterResponse =
                new SuccessfulCrudRequester<EncounterResponse>(
                        RequestSpecs.adminSpec(),
                        Endpoint.ENCOUNTER_RETRIEVE,
                        ResponseSpecs.requestReturnsOk())
                        .get(encounterResponse.getUuid(),
                                GetParams.builder()
                                        .v(GetParams.FULL)
                                        .build()
                                        .toQueryParams());

        ModelAssertions.assertThatModels(encounterRequest, receivedEncounterResponse).match();

        softly.assertThat(receivedEncounterResponse.getVoided()).isFalse();
        softly.assertThat(receivedEncounterResponse.getPatient().getDisplay())
                .isEqualTo(SessionStorage.getPatient().getDisplay());
    }

    @Test
    @CreatePatient
    public void adminCannotCreateEncounterWithInvalidPatient() {
        CreateEncounterRequest encounterRequest = RandomModelGenerator.generate(CreateEncounterRequest.class);
        encounterRequest.setPatient(null);

        new CrudRequester(
                RequestSpecs.adminSpec(),
                Endpoint.ENCOUNTER_POST,
                ResponseSpecs.requestReturnsBadRequestWithMessage(MISSING_PATIENT.getMessage()))
                .create(encounterRequest);

        encounterRequest.setPatient(nonExistingUuid);

        new CrudRequester(
                RequestSpecs.adminSpec(),
                Endpoint.ENCOUNTER_POST,
                ResponseSpecs.requestReturnsBadRequestWithTwoMessages(
                        INVALID_SUBMISSION.getMessage(),
                        PATIENT_IS_REQUIRES.getMessage()))
                .create(encounterRequest);
    }

    @Test
    @CreatePatient
    public void adminCannotCreateEncounterWithoutEncounterType() {
        CreateEncounterRequest encounterRequest = RandomModelGenerator.generate(CreateEncounterRequest.class);
        encounterRequest.setEncounterType(null);

        new CrudRequester(
                RequestSpecs.adminSpec(),
                Endpoint.ENCOUNTER_POST,
                ResponseSpecs.requestReturnsBadRequestWithMessage(MISSING_ENCOUNTER_TYPE.getMessage()))
                .create(encounterRequest);
    }

    @Test
    @CreatePatient
    public void adminCannotCreateEncounterWithFutureDatetime() {
        CreateEncounterRequest encounterRequest = RandomModelGenerator.generate(CreateEncounterRequest.class);
        encounterRequest.setEncounterDatetime(RandomModelGenerator.futureDateTime());

        new CrudRequester(
                RequestSpecs.adminSpec(),
                Endpoint.ENCOUNTER_POST,
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
        CreateEncounterRequest encounterRequest = RandomModelGenerator.generate(CreateEncounterRequest.class);

        EncounterResponse encounterResponse = new SuccessfulCrudRequester<EncounterResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.ENCOUNTER_POST,
                ResponseSpecs.requestReturnsCreated())
                .create(encounterRequest);

        encounterRequest.setEncounterDatetime(RandomModelGenerator.pastDateTime());

        EncounterResponse updatedEncounterResponse = new SuccessfulCrudRequester<EncounterResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.ENCOUNTER_POST,
                ResponseSpecs.requestReturnsOk())
                .update(encounterResponse.getUuid(), encounterRequest);

        EncounterResponse receivedEncounterResponse =
                new SuccessfulCrudRequester<EncounterResponse>(
                        RequestSpecs.adminSpec(),
                        Endpoint.ENCOUNTER_RETRIEVE,
                        ResponseSpecs.requestReturnsOk())
                        .get(encounterResponse.getUuid(),
                                GetParams.builder()
                                        .v(GetParams.FULL)
                                        .build()
                                        .toQueryParams());

        ModelAssertions.assertThatModels(encounterRequest, receivedEncounterResponse).match();
        softly.assertThat(updatedEncounterResponse.getUuid()).isEqualTo(encounterResponse.getUuid());
        softly.assertThat(updatedEncounterResponse.getVoided()).isFalse();
    }

    @Test
    @CreatePatient
    public void adminCannotUpdateNonExistingEncounter() {
        CreateEncounterRequest updateRequest = RandomModelGenerator.generate(CreateEncounterRequest.class);
        updateRequest.setEncounterDatetime(RandomModelGenerator.pastDateTime());

        new CrudRequester(
                RequestSpecs.adminSpec(),
                Endpoint.ENCOUNTER_POST,
                ResponseSpecs.requestReturnsNotFound(
                        OBJECT_WITH_UUID_DOES_NOT_EXIST.getMessage()))
                .update(nonExistingUuid, updateRequest);
    }

    @Test
    @CreatePatient
    public void adminCanDeleteEncounter() {
        CreateEncounterRequest encounterRequest = RandomModelGenerator.generate(CreateEncounterRequest.class);

        EncounterResponse encounterResponse = new SuccessfulCrudRequester<EncounterResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.ENCOUNTER_POST,
                ResponseSpecs.requestReturnsCreated())
                .create(encounterRequest);

        new SuccessfulCrudRequester<EncounterResponse>(
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