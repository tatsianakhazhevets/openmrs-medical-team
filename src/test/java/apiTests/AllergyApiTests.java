package apiTests;

import apiParts.models.allergy.*;
import apiParts.models.patient.CreatePatientResponse;
import apiParts.skelethon.endpoints.Endpoint;
import apiParts.skelethon.requests.allergy.AllergyRequester;
import apiParts.skelethon.requests.allergy.SuccessfulAllergyRequester;
import apiParts.skelethon.requests.nested.SuccessfulNestedCrudRequester;
import apiParts.specs.RequestSpecs;
import apiParts.specs.ResponseSpecs;
import apiParts.steps.AdminSteps;
import apiParts.testdata.AllergyTestData;
import common.annotations.CreatePatient;
import common.storages.SessionStorage;
import net.datafaker.Faker;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Locale;
import java.util.UUID;

import static apiParts.models.errors.AllergyErrorMassages.*;

@CreatePatient
public class AllergyApiTests extends BaseTest {

    Faker faker = new Faker(new Locale("en", "US"));

    String nonExistingUuid = UUID.randomUUID().toString();
    String updatedComment = faker.lorem().sentence();
    String comment = faker.lorem().sentence();

    @Test
    public void adminCanCreateAllergy() {
        String patientUUID = SessionStorage.getPatient().getUuid();

        AllergyRequest allergyRequest = AllergyTestData.allergyRequest(comment);

        AllergyResponse createdAllergyResponse = new SuccessfulAllergyRequester<AllergyResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.ALLERGY_POST,
                ResponseSpecs.requestReturnsCreated())
                .create(patientUUID, allergyRequest);

        softly.assertThat(createdAllergyResponse.getUuid()).isNotNull();
        softly.assertThat(createdAllergyResponse.getComment()).isEqualTo(comment);

        AllergyResponse getAllergyResponse = new SuccessfulAllergyRequester<AllergyResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.ALLERGY_POST,
                ResponseSpecs.requestReturnsOk())
                .get(patientUUID, createdAllergyResponse.getUuid());

        softly.assertThat(getAllergyResponse.getUuid()).isEqualTo(createdAllergyResponse.getUuid());
        softly.assertThat(getAllergyResponse.getComment()).isEqualTo(comment);
    }

    @Test
    public void adminCannotCreateAllergyWithoutAllergen() {
        String patientUUID = SessionStorage.getPatient().getUuid();

        AllergyRequest request = AllergyRequest.builder()
                .severity(Severity.builder()
                        .uuid(SeverityUuid.SEVERITY_UUID.getSeverity())
                        .build())
                .comment(comment)
                .build();

        new AllergyRequester(
                RequestSpecs.adminSpec(),
                Endpoint.ALLERGY_POST,
                ResponseSpecs.requestReturnsBadRequestWithMessage(INVALID_SUBMISSION.getMessage()))
                .create(patientUUID, request);
    }

    @Test
    public void adminCannotCreateAllergyWithInvalidAllergenUuid() {
        String patientUUID = SessionStorage.getPatient().getUuid();

        AllergyRequest request = AllergyRequest.builder()
                .allergen(Allergen.builder()
                        .allergenType(AllergenType.DRUG.getDrug())
                        .codedAllergen(CodedAllergen.builder()
                                .uuid(nonExistingUuid)
                                .build())
                        .build())
                .severity(Severity.builder()
                        .uuid(SeverityUuid.SEVERITY_UUID.getSeverity())
                        .build())
                .comment(comment)
                .build();

        new AllergyRequester(
                RequestSpecs.adminSpec(),
                Endpoint.ALLERGY_POST,
                ResponseSpecs.requestReturnsBadRequestWithMessage(
                        SHOULD_USE_NEW_DELEGATE.getMessage()))
                .create(patientUUID, request);
    }

    @Test
    public void adminCannotGetNonExistingAllergy() {
        String patientUUID = SessionStorage.getPatient().getUuid();

        new AllergyRequester(
                RequestSpecs.adminSpec(),
                Endpoint.ALLERGY_POST,
                ResponseSpecs.requestReturnsNotFound(OBJECT_WITH_UUID_DOES_NOT_EXIST.getMessage()))
                .get(patientUUID, nonExistingUuid);
    }

    @Test
    public void adminCannotGetAllergyForNonExistingPatient() {
        new AllergyRequester(
                RequestSpecs.adminSpec(),
                Endpoint.ALLERGY_POST,
                ResponseSpecs.requestReturnsNotFound(OBJECT_WITH_UUID_DOES_NOT_EXIST.getMessage()))
                .get(nonExistingUuid, nonExistingUuid);
    }

    @Test
    public void adminCanUpdateAllergy() {
        String patientUUID = SessionStorage.getPatient().getUuid();

        AllergyRequest allergyRequest = AllergyTestData.allergyRequest(comment);

        AllergyResponse createdAllergyResponse = new SuccessfulAllergyRequester<AllergyResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.ALLERGY_POST,
                ResponseSpecs.requestReturnsCreated())
                .create(patientUUID, allergyRequest);

        AllergyRequest updateAllergyRequest = AllergyTestData.allergyRequest(updatedComment);

        AllergyResponse updatedAllergyResponse = new SuccessfulAllergyRequester<AllergyResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.ALLERGY_POST,
                ResponseSpecs.requestReturnsOk())
                .update(patientUUID, createdAllergyResponse.getUuid(), updateAllergyRequest);

        softly.assertThat(updatedAllergyResponse.getUuid()).isEqualTo(createdAllergyResponse.getUuid());
        softly.assertThat(updatedAllergyResponse.getComment()).isEqualTo(updatedComment);

        AllergyResponse getAllergyResponse = new SuccessfulAllergyRequester<AllergyResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.ALLERGY_POST,
                ResponseSpecs.requestReturnsOk())
                .get(patientUUID, createdAllergyResponse.getUuid());

        softly.assertThat(getAllergyResponse.getUuid()).isEqualTo(createdAllergyResponse.getUuid());
        softly.assertThat(getAllergyResponse.getComment()).isEqualTo(updatedComment);
    }

    @Test
    public void adminCannotUpdateNonExistingAllergy() {
        String patientUUID = SessionStorage.getPatient().getUuid();

        AllergyRequest request = AllergyTestData.allergyRequest(updatedComment);

        new AllergyRequester(
                RequestSpecs.adminSpec(),
                Endpoint.ALLERGY_POST,
                ResponseSpecs.requestReturnsNotFound(OBJECT_WITH_UUID_DOES_NOT_EXIST.getMessage()))
                .update(patientUUID, nonExistingUuid, request);
    }

    @Test
    public void adminCanDeleteAllergy() {
        String patientUUID = SessionStorage.getPatient().getUuid();

        AllergyRequest allergyRequest = AllergyTestData.allergyRequest(comment);

        AllergyResponse createdAllergyResponse = new SuccessfulAllergyRequester<AllergyResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.ALLERGY_POST,
                ResponseSpecs.requestReturnsCreated())
                .create(patientUUID, allergyRequest);

        softly.assertThat(createdAllergyResponse.getUuid()).isNotNull();
        softly.assertThat(createdAllergyResponse.getComment()).isEqualTo(comment);

        new SuccessfulAllergyRequester<AllergyResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.ALLERGY_POST,
                ResponseSpecs.requestReturnsNoContent())
                .delete(patientUUID, createdAllergyResponse.getUuid());

        AllergyResponse deletedAllergyResponse = new SuccessfulAllergyRequester<AllergyResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.ALLERGY_POST,
                ResponseSpecs.requestReturnsOk())
                .get(patientUUID, createdAllergyResponse.getUuid());

        softly.assertThat(deletedAllergyResponse.getUuid()).isEqualTo(createdAllergyResponse.getUuid());
        softly.assertThat(deletedAllergyResponse.getVoided()).isTrue();
    }

    @Test
    public void adminCannotDeleteNonExistingAllergy() {
        String patientUUID = SessionStorage.getPatient().getUuid();

        new SuccessfulAllergyRequester<AllergyResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.ALLERGY_POST,
                ResponseSpecs.requestReturnsNotFound(OBJECT_WITH_UUID_DOES_NOT_EXIST.getMessage()))
                .delete(
                        patientUUID, nonExistingUuid);
    }

    // ==== COPY of adminCanCreateAllergy built on NestedCrudRequester. Original untouched. ====
    // Differences from the original:
    //  - AllergyRequester/SuccessfulAllergyRequester replaced by one generic requester;
    //  - the url comes from Endpoint.PATIENT_ALLERGY_NESTED instead of being hardcoded
    //    inside the requester class;
    //  - v=full is passed explicitly by the test instead of being hidden
    //    inside AllergyRequester.get().
    @Test
    public void adminCanCreateAllergyViaNestedCrud() {
        CreatePatientResponse createdPatientResponse = AdminSteps.createPatient();

        AllergyRequest allergyRequest = AllergyRequest.builder()
                .allergen(Allergen.builder()
                        .allergenType(AllergenType.DRUG.getDrug())
                        .codedAllergen(CodedAllergen.builder()
                                .uuid(CodedAllergenUuid.ALLERGEN_UUID.getAllergen())
                                .build())
                        .build())
                .severity(Severity.builder()
                        .uuid(SeverityUuid.SEVERITY_UUID.getSeverity())
                        .build())
                .comment(comment)
                .reactions(List.of(
                        ReactionWrapper.builder()
                                .reaction(Reaction.builder()
                                        .uuid(ReactionUuid.REACTION_UUID.getReaction())
                                        .build())
                                .build()))
                .build();

        // POST /patient/{patientUuid}/allergy
        AllergyResponse createdAllergyResponse = new SuccessfulNestedCrudRequester<AllergyResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.PATIENT_ALLERGY_NESTED,
                ResponseSpecs.requestReturnsCreated())
                .create(createdPatientResponse.getUuid(), allergyRequest);

        softly.assertThat(createdAllergyResponse.getUuid()).isNotNull();
        softly.assertThat(createdAllergyResponse.getComment()).isEqualTo(comment);

        // GET /patient/{patientUuid}/allergy/{allergyUuid}?v=full
        AllergyResponse getAllergyResponse = new SuccessfulNestedCrudRequester<AllergyResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.PATIENT_ALLERGY_NESTED,
                ResponseSpecs.requestReturnsOk())
                .get(createdPatientResponse.getUuid(),
                        createdAllergyResponse.getUuid(),
                        Map.of("v", "full"));

        softly.assertThat(getAllergyResponse.getUuid()).isEqualTo(createdAllergyResponse.getUuid());
        softly.assertThat(getAllergyResponse.getComment()).isEqualTo(comment);
    }

}
