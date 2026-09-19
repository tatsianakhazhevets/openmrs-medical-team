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
import net.datafaker.Faker;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Locale;
import java.util.UUID;

import static apiParts.models.errors.AllergyErrorMassages.*;

public class AllergyApiTests extends BaseTest {

    Faker faker = new Faker(new Locale("en", "US"));

    String nonExistingUuid = UUID.randomUUID().toString();;
    String updatedComment = faker.lorem().sentence();
    String comment = faker.lorem().sentence();

    @Test
    public void adminCanCreateAllergy() {
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

        AllergyResponse createdAllergyResponse = new SuccessfulAllergyRequester<AllergyResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.ALLERGY_POST,
                ResponseSpecs.requestReturnsCreated())
                .create(createdPatientResponse.getUuid(), allergyRequest);

        softly.assertThat(createdAllergyResponse.getUuid()).isNotNull();
        softly.assertThat(createdAllergyResponse.getComment()).isEqualTo(comment);

        AllergyResponse getAllergyResponse = new SuccessfulAllergyRequester<AllergyResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.ALLERGY_POST,
                ResponseSpecs.requestReturnsOk())
                .get(createdPatientResponse.getUuid(), createdAllergyResponse.getUuid());

        softly.assertThat(getAllergyResponse.getUuid()).isEqualTo(createdAllergyResponse.getUuid());
        softly.assertThat(getAllergyResponse.getComment()).isEqualTo(comment);
    }

    @Test
    public void adminCannotCreateAllergyWithoutAllergen() {
        CreatePatientResponse patient = AdminSteps.createPatient();

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
                .create(patient.getUuid(), request);
    }

    @Test
    public void adminCannotCreateAllergyWithInvalidAllergenUuid() {
        CreatePatientResponse patient = AdminSteps.createPatient();

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
                .create(patient.getUuid(), request);
    }

    @Test
    public void adminCannotGetNonExistingAllergy() {
        CreatePatientResponse patient = AdminSteps.createPatient();

        new AllergyRequester(
                RequestSpecs.adminSpec(),
                Endpoint.ALLERGY_POST,
                ResponseSpecs.requestReturnsNotFound(OBJECT_WITH_UUID_DOES_NOT_EXIST.getMessage()))
                .get(patient.getUuid(), nonExistingUuid);
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

        AllergyResponse createdAllergyResponse = new SuccessfulAllergyRequester<AllergyResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.ALLERGY_POST,
                ResponseSpecs.requestReturnsCreated())
                .create(createdPatientResponse.getUuid(), allergyRequest);

        AllergyRequest updateAllergyRequest = AllergyRequest.builder()
                .allergen(Allergen.builder()
                        .allergenType(AllergenType.DRUG.getDrug())
                        .codedAllergen(CodedAllergen.builder()
                                .uuid(CodedAllergenUuid.ALLERGEN_UUID.getAllergen())
                                .build())
                        .build())
                .severity(Severity.builder()
                        .uuid(SeverityUuid.SEVERITY_UUID.getSeverity())
                        .build())
                .comment(updatedComment)
                .reactions(List.of(
                        ReactionWrapper.builder()
                                .reaction(Reaction.builder()
                                        .uuid(ReactionUuid.REACTION_UUID.getReaction())
                                        .build())
                                .build()))
                .build();

        AllergyResponse updatedAllergyResponse = new SuccessfulAllergyRequester<AllergyResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.ALLERGY_POST,
                ResponseSpecs.requestReturnsOk())
                .update(createdPatientResponse.getUuid(), createdAllergyResponse.getUuid(), updateAllergyRequest);

        softly.assertThat(updatedAllergyResponse.getUuid()).isEqualTo(createdAllergyResponse.getUuid());
        softly.assertThat(updatedAllergyResponse.getComment()).isEqualTo(updatedComment);

        AllergyResponse getAllergyResponse = new SuccessfulAllergyRequester<AllergyResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.ALLERGY_POST,
                ResponseSpecs.requestReturnsOk())
                .get(createdPatientResponse.getUuid(), createdAllergyResponse.getUuid());

        softly.assertThat(getAllergyResponse.getUuid()).isEqualTo(createdAllergyResponse.getUuid());
        softly.assertThat(getAllergyResponse.getComment()).isEqualTo(updatedComment);
    }

    @Test
    public void adminCannotUpdateNonExistingAllergy() {
        CreatePatientResponse patient = AdminSteps.createPatient();

        AllergyRequest request = AllergyRequest.builder()
                .allergen(Allergen.builder()
                        .allergenType(AllergenType.DRUG.getDrug())
                        .codedAllergen(CodedAllergen.builder()
                                .uuid(CodedAllergenUuid.ALLERGEN_UUID.getAllergen())
                                .build())
                        .build())
                .severity(Severity.builder()
                        .uuid(SeverityUuid.SEVERITY_UUID.getSeverity())
                        .build())
                .comment(updatedComment)
                .build();

        new AllergyRequester(
                RequestSpecs.adminSpec(),
                Endpoint.ALLERGY_POST,
                ResponseSpecs.requestReturnsNotFound(OBJECT_WITH_UUID_DOES_NOT_EXIST.getMessage()))
                .update(patient.getUuid(), nonExistingUuid, request);
    }

    @Test
    public void adminCanDeleteAllergy() {
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

        AllergyResponse createdAllergyResponse = new SuccessfulAllergyRequester<AllergyResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.ALLERGY_POST,
                ResponseSpecs.requestReturnsCreated())
                .create(createdPatientResponse.getUuid(), allergyRequest);

        softly.assertThat(createdAllergyResponse.getUuid()).isNotNull();
        softly.assertThat(createdAllergyResponse.getComment()).isEqualTo(comment);

        new SuccessfulAllergyRequester<AllergyResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.ALLERGY_POST,
                ResponseSpecs.requestReturnsNoContent())
                .delete(createdPatientResponse.getUuid(), createdAllergyResponse.getUuid());

        AllergyResponse deletedAllergyResponse = new SuccessfulAllergyRequester<AllergyResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.ALLERGY_POST,
                ResponseSpecs.requestReturnsOk())
                .get(createdPatientResponse.getUuid(), createdAllergyResponse.getUuid());

        softly.assertThat(deletedAllergyResponse.getUuid()).isEqualTo(createdAllergyResponse.getUuid());
        softly.assertThat(deletedAllergyResponse.getVoided()).isTrue();
    }

    @Test
    public void adminCannotDeleteNonExistingAllergy() {
        CreatePatientResponse patient = AdminSteps.createPatient();

        new SuccessfulAllergyRequester<AllergyResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.ALLERGY_POST,
                ResponseSpecs.requestReturnsNotFound(OBJECT_WITH_UUID_DOES_NOT_EXIST.getMessage()))
                .delete(
                        patient.getUuid(), nonExistingUuid);
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
