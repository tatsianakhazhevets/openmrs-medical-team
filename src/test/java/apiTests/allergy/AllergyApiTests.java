package apiTests.allergy;

import apiParts.assertions.ModelAssertions;
import apiParts.generators.RandomModelGenerator;
import apiParts.generators.RandomUuidGenerator;
import apiParts.models.GetParams;
import apiParts.models.allergy.*;
import apiParts.skelethon.endpoints.Endpoint;
import apiParts.skelethon.requests.nestedCrud.NestedCrudRequester;
import apiParts.skelethon.requests.nestedCrud.SuccessfulNestedCrudRequester;
import apiParts.specs.RequestSpecs;
import apiParts.specs.ResponseSpecs;
import apiTests.BaseTest;
import common.annotations.CreatePatient;
import common.storages.SessionStorage;
import io.restassured.response.ValidatableResponse;
import org.junit.jupiter.api.Test;

import static apiParts.models.errors.AllergyErrorMessages.*;

@CreatePatient
public class AllergyApiTests extends BaseTest {

    String nonExistingUuid = RandomUuidGenerator.generateUuid();

    @Test
    @CreatePatient
    public void adminCanCreateAllergy() {
        AllergyRequest allergyRequest = RandomModelGenerator.generate(AllergyRequest.class);

        AllergyResponse createdAllergyResponse = new SuccessfulNestedCrudRequester<AllergyResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.PATIENT_ALLERGY_NESTED,
                ResponseSpecs.requestReturnsCreated())
                .create(SessionStorage.getPatient().getUuid(), allergyRequest);

        ModelAssertions.assertThatModels(softly, allergyRequest, createdAllergyResponse).match();
        softly.assertThat(createdAllergyResponse.getUuid()).isNotNull();

        AllergyResponse getAllergyResponse = new SuccessfulNestedCrudRequester<AllergyResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.PATIENT_ALLERGY_NESTED,
                ResponseSpecs.requestReturnsOk())
                .get(SessionStorage.getPatient().getUuid(),
                        createdAllergyResponse.getUuid(),
                        GetParams.builder()
                                .v(GetParams.FULL)
                                .build()
                                .toQueryParams());

        ModelAssertions.assertThatModels(softly, allergyRequest, getAllergyResponse).match();
        softly.assertThat(getAllergyResponse.getUuid()).isEqualTo(createdAllergyResponse.getUuid());
    }

    @Test
    @CreatePatient
    public void adminCannotCreateAllergyWithoutAllergen() {
        AllergyRequest allergyRequest = RandomModelGenerator.generate(AllergyRequest.class);
        allergyRequest.setAllergen(null);

        ValidatableResponse response = new NestedCrudRequester(
                RequestSpecs.adminSpec(),
                Endpoint.PATIENT_ALLERGY_NESTED,
                ResponseSpecs.requestReturnsBadRequestWithTwoMessages(
                        INVALID_SUBMISSION.getMessage(),
                        ALLERGEN_REQUIRED.getMessage()))
                .create(SessionStorage.getPatient().getUuid(), allergyRequest);

    }

    @Test
    @CreatePatient
    public void adminCannotCreateAllergyWithInvalidAllergenUuid() {
        AllergyRequest allergyRequest = RandomModelGenerator.generate(AllergyRequest.class);
        allergyRequest.getAllergen().setCodedAllergen(CodedAllergen.builder()
                .uuid(nonExistingUuid)
                .build());

        new NestedCrudRequester(
                RequestSpecs.adminSpec(),
                Endpoint.PATIENT_ALLERGY_NESTED,
                ResponseSpecs.requestReturnsBadRequestWithMessage(
                        SHOULD_USE_NEW_DELEGATE.getMessage()))
                .create(SessionStorage.getPatient().getUuid(), allergyRequest);
    }

    @Test
    public void adminCannotCreateAllergyForNonExistingPatient() {
        AllergyRequest allergyRequest = RandomModelGenerator.generate(AllergyRequest.class);

        new NestedCrudRequester(
                RequestSpecs.adminSpec(),
                Endpoint.PATIENT_ALLERGY_NESTED,
                ResponseSpecs.requestReturnsNotFound(
                        OBJECT_WITH_UUID_DOES_NOT_EXIST.getMessage()))
                .create(nonExistingUuid, allergyRequest);
    }

    @Test
    @CreatePatient
    public void adminCannotGetNonExistingAllergy() {

        new NestedCrudRequester(
                RequestSpecs.adminSpec(),
                Endpoint.PATIENT_ALLERGY_NESTED,
                ResponseSpecs.requestReturnsNotFound(
                        OBJECT_WITH_UUID_DOES_NOT_EXIST.getMessage()))
                .get(SessionStorage.getPatient().getUuid(),
                        nonExistingUuid,
                        GetParams.builder()
                        .v(GetParams.FULL)
                        .build()
                        .toQueryParams());
    }

    @Test
    public void adminCannotGetAllergyForNonExistingPatient() {

        new NestedCrudRequester(
                RequestSpecs.adminSpec(),
                Endpoint.PATIENT_ALLERGY_NESTED,
                ResponseSpecs.requestReturnsNotFound())
                .get(nonExistingUuid, nonExistingUuid,
                        GetParams.builder()
                                .v(GetParams.FULL)
                                .build()
                                .toQueryParams());
    }

    @Test
    @CreatePatient
    public void adminCanUpdateAllergy() {
        AllergyRequest allergyRequest = RandomModelGenerator.generate(AllergyRequest.class);

        AllergyResponse createdAllergyResponse = new SuccessfulNestedCrudRequester<AllergyResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.PATIENT_ALLERGY_NESTED,
                ResponseSpecs.requestReturnsCreated())
                .create(SessionStorage.getPatient().getUuid(), allergyRequest);

        allergyRequest.setComment(RandomModelGenerator.randomWord());

        AllergyResponse updatedAllergyResponse = new SuccessfulNestedCrudRequester<AllergyResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.PATIENT_ALLERGY_NESTED,
                ResponseSpecs.requestReturnsOk())
                .update(SessionStorage.getPatient().getUuid(), createdAllergyResponse.getUuid(),
                        allergyRequest);

        ModelAssertions.assertThatModels(softly, allergyRequest, updatedAllergyResponse).match();
        softly.assertThat(updatedAllergyResponse.getUuid()).isEqualTo(createdAllergyResponse.getUuid());

        AllergyResponse getAllergyResponse = new SuccessfulNestedCrudRequester<AllergyResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.PATIENT_ALLERGY_NESTED,
                ResponseSpecs.requestReturnsOk())
                .get(SessionStorage.getPatient().getUuid(),
                        createdAllergyResponse.getUuid(),
                        GetParams.builder()
                                .v(GetParams.FULL)
                                .build()
                                .toQueryParams());

        softly.assertThat(getAllergyResponse.getUuid()).isEqualTo(createdAllergyResponse.getUuid());
        softly.assertThat(getAllergyResponse.getComment()).isEqualTo(updatedAllergyResponse.getComment());
    }

    @Test
    @CreatePatient
    public void adminCannotUpdateNonExistingAllergy() {
        AllergyRequest allergyRequest = RandomModelGenerator.generate(AllergyRequest.class);

        new NestedCrudRequester(
                RequestSpecs.adminSpec(),
                Endpoint.PATIENT_ALLERGY_NESTED,
                ResponseSpecs.requestReturnsNotFound(OBJECT_WITH_UUID_DOES_NOT_EXIST.getMessage()))
                .update(SessionStorage.getPatient().getUuid(), nonExistingUuid, allergyRequest);
    }

    @Test
    @CreatePatient
    public void adminCanDeleteAllergy() {
        AllergyRequest allergyRequest = RandomModelGenerator.generate(AllergyRequest.class);

        AllergyResponse createdAllergyResponse = new SuccessfulNestedCrudRequester<AllergyResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.PATIENT_ALLERGY_NESTED,
                ResponseSpecs.requestReturnsCreated())
                .create(SessionStorage.getPatient().getUuid(), allergyRequest);

        softly.assertThat(createdAllergyResponse.getUuid()).isNotNull();

        new SuccessfulNestedCrudRequester<AllergyResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.PATIENT_ALLERGY_NESTED,
                ResponseSpecs.requestReturnsNoContent())
                .delete(SessionStorage.getPatient().getUuid(), createdAllergyResponse.getUuid());
    }

    @Test
    public void adminCannotDeleteNonExistingAllergy() {

        new NestedCrudRequester(
                RequestSpecs.adminSpec(),
                Endpoint.PATIENT_ALLERGY_NESTED,
                ResponseSpecs.requestReturnsNotFound(OBJECT_WITH_UUID_DOES_NOT_EXIST.getMessage()))
                .delete(SessionStorage.getPatient().getUuid(), nonExistingUuid);
    }
}