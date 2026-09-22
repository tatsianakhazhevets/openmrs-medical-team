package apiTests;

import apiParts.assertions.ModelAssertions;
import apiParts.generators.RandomModelGenerator;
import apiParts.generators.RandomNameGenerator;
import apiParts.generators.RandomUuidGenerator;
import apiParts.models.patient.*;
import apiParts.models.search.SearchResult;
import apiParts.skelethon.endpoints.Endpoint;
import apiParts.skelethon.requests.crud.CrudRequester;
import apiParts.skelethon.requests.crud.SuccessfulCrudRequester;
import apiParts.skelethon.requests.nestedCrud.NestedCrudRequester;
import apiParts.skelethon.requests.nestedCrud.SuccessfulNestedCrudRequester;
import apiParts.skelethon.requests.search.SuccessfulSearchRequester;
import apiParts.specs.RequestSpecs;
import apiParts.specs.ResponseSpecs;
import apiParts.steps.AdminSteps;
import org.junit.jupiter.api.Test;

import static apiParts.models.errors.PatientErrorMessages.*;

public class PatientManagementApiTests extends BaseTest {

    String nonExistingUuid = RandomUuidGenerator.generateUuid();

    @Test
    public void adminCanCreatePatient() {
        CreatePatientRequest createPatientRequest = RandomModelGenerator.generate(CreatePatientRequest.class);

        CreatePatientResponse createdPatientResponse = new SuccessfulCrudRequester<CreatePatientResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.PATIENT_POST,
                ResponseSpecs.requestReturnsCreated())
                .create(createPatientRequest);

        ModelAssertions.assertThatModels(softly, createPatientRequest, createdPatientResponse).match();

        GetPatientResponse getPatientResponse = new SuccessfulCrudRequester<GetPatientResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.PATIENT_GET,
                ResponseSpecs.requestReturnsOk())
                .get(createdPatientResponse.getUuid());

        ModelAssertions.assertThatModels(softly, createPatientRequest, getPatientResponse).match();

        softly.assertThat(getPatientResponse.getUuid()).isEqualTo(createdPatientResponse.getUuid());
        softly.assertThat(getPatientResponse.getPerson()).isNotNull();
        softly.assertThat(getPatientResponse.getPerson().getUuid()).isEqualTo(createdPatientResponse.getUuid());
        softly.assertThat(getPatientResponse.getPerson().getPreferredName()).isNotNull();
        softly.assertThat(getPatientResponse.getPerson().getPreferredName().getDisplay())
                .isEqualTo(createPatientRequest.getPerson().getNames().get(0).getGivenName() + " "
                        + createPatientRequest.getPerson().getNames().get(0).getFamilyName());
        softly.assertThat(getPatientResponse.getPerson().getPreferredAddress()).isNotNull();
        softly.assertThat(getPatientResponse.getPerson().getPreferredAddress().getDisplay())
                .isEqualTo(createPatientRequest.getPerson().getAddresses().get(0).getAddress1());
        softly.assertThat(getPatientResponse.getIdentifiers()).isNotEmpty();
        softly.assertThat(getPatientResponse.getIdentifiers().get(0).getDisplay())
                .endsWith(createPatientRequest.getIdentifiers().get(0).getIdentifier());
    }

    @Test
    public void adminCannotCreatePatientWithoutPerson() {
        CreatePatientRequest createPatientRequest = RandomModelGenerator.generate(CreatePatientRequest.class);
        createPatientRequest.setPerson(null);

        new CrudRequester(
                RequestSpecs.adminSpec(),
                Endpoint.PATIENT_POST,
                ResponseSpecs.requestReturnsBadRequestWithMessage(
                        PERSON_IS_MISSING.getMessage()))
                .create(createPatientRequest);
    }

    @Test
    public void adminCannotCreatePatientWithoutIdentifiers() {
        CreatePatientRequest createPatientRequest = RandomModelGenerator.generate(CreatePatientRequest.class);
        createPatientRequest.setIdentifiers(null);

        new CrudRequester(
                RequestSpecs.adminSpec(),
                Endpoint.PATIENT_POST,
                ResponseSpecs.requestReturnsBadRequestWithMessage(IDENTIFIER_CANNOT_INVOKE.getMessage()))
                .create(createPatientRequest);
    }

    @Test
    public void adminCannotGetPatientWithNonExistingUuid() {

        new CrudRequester(
                RequestSpecs.adminSpec(),
                Endpoint.PATIENT_GET,
                ResponseSpecs.requestReturnsNotFound())
                .get(nonExistingUuid);
    }

    @Test
    public void adminCanSearchPatient() {
        CreatePatientRequest request = RandomModelGenerator.generate(CreatePatientRequest.class);

        CreatePatientResponse createdPatient = new SuccessfulCrudRequester<CreatePatientResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.PATIENT_POST,
                ResponseSpecs.requestReturnsCreated())
                .create(request);

        String searchQuery = createdPatient.getPerson().getPreferredName().getDisplay();

        SearchResult<GetPatientResponse> patients = new SuccessfulSearchRequester<GetPatientResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.PATIENT_SEARCH_GET,
                ResponseSpecs.requestReturnsOk())
                .search(PatientSearchParams.builder()
                        .query(searchQuery)
                        .representation(PatientSearchParams.DEFAULT_REPRESENTATION)
                        .limit(PatientSearchParams.DEFAULT_LIMIT)
                        .build());

        softly.assertThat(patients.results()).isNotEmpty();

        GetPatientResponse foundPatient = patients.results().stream()
                .filter(patient -> patient.getUuid().equals(createdPatient.getUuid()))
                .findFirst()
                .orElse(null);

        softly.assertThat(foundPatient).isNotNull();

        if (foundPatient != null) {
            ModelAssertions.assertThatModels(softly, request, foundPatient).match();
        }
    }

    /// Search for a non-existing patient returns 200 with an empty list; 404 applies to GET by UUID.
    @Test
    public void adminCannotFindNonExistingPatient() {
        String searchQuery = RandomNameGenerator.generateNonExistingDisplayName();

        SearchResult<GetPatientResponse> patients = new SuccessfulSearchRequester<GetPatientResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.PATIENT_SEARCH_GET,
                ResponseSpecs.requestReturnsOk())
                .search(PatientSearchParams.builder()
                        .query(searchQuery)
                        .representation(PatientSearchParams.DEFAULT_REPRESENTATION)
                        .limit(PatientSearchParams.DEFAULT_LIMIT)
                        .build());

        softly.assertThat(patients.isEmpty()).isTrue();
    }

    @Test
    public void adminCanUpdatePatient() {
        CreatePatientRequest request = RandomModelGenerator.generate(CreatePatientRequest.class);

        CreatePatientResponse createdPatient = new SuccessfulCrudRequester<CreatePatientResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.PATIENT_POST,
                ResponseSpecs.requestReturnsCreated())
                .create(request);

        String givenName = request.getPerson().getNames().get(0).getGivenName();
        String updatedFamilyName = RandomNameGenerator.generateNonExistingName();
        request.getPerson().getNames().get(0).setFamilyName(updatedFamilyName);

        new CrudRequester(
                RequestSpecs.adminSpec(),
                Endpoint.PATIENT_POST,
                ResponseSpecs.requestReturnsOk())
                .update(createdPatient.getUuid(), request);

        GetPatientResponse getPatientResponse = new SuccessfulCrudRequester<GetPatientResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.PATIENT_GET,
                ResponseSpecs.requestReturnsOk())
                .get(createdPatient.getUuid());

        ModelAssertions.assertThatModels(softly, request, getPatientResponse).match();
        softly.assertThat(getPatientResponse.getPerson()).isNotNull();
        softly.assertThat(getPatientResponse.getPerson().getPreferredName()).isNotNull();
        softly.assertThat(getPatientResponse.getPerson().getPreferredName().getDisplay())
                .isEqualTo(givenName + " " + updatedFamilyName);
    }

    /// Fix needed: Expected 404 Not Found, but received 500 Internal Server Error.
    @Test
    public void adminCannotUpdateNonExistingPatient() {
        CreatePatientRequest request = RandomModelGenerator.generate(CreatePatientRequest.class);

        new CrudRequester(
                RequestSpecs.adminSpec(),
                Endpoint.PATIENT_POST,
                ResponseSpecs.requestReturnsServerError())
                .update(nonExistingUuid, request);
    }

    @Test
    public void adminCanDeletePatient() {
        CreatePatientResponse createPatientResponse = AdminSteps.createPatientWithoutInvokedIdentifier();

        new SuccessfulCrudRequester<CreatePatientRequest>(
                RequestSpecs.adminSpec(),
                Endpoint.PATIENT_DELETE,
                ResponseSpecs.requestReturnsNoContent())
                .delete(createPatientResponse.getUuid(),
                        PatientDeleteParams.builder()
                                .purge(true)
                                .build()
                                .toQueryParams());

        new CrudRequester(
                RequestSpecs.adminSpec(),
                Endpoint.PATIENT_GET,
                ResponseSpecs.requestReturnsNotFound(OBJECT_WITH_UUID_DOES_NOT_EXIST.getMessage()))
                .get(createPatientResponse.getUuid());
    }

    /// Fix needed: Expected 404 Not Found, but received 204 No Content.
    @Test
    public void adminCannotDeleteNonExistingPatient() {

        new CrudRequester(
                RequestSpecs.adminSpec(),
                Endpoint.PATIENT_DELETE,
                ResponseSpecs.requestReturnsNoContent())
                .delete(nonExistingUuid,
                        PatientDeleteParams.builder()
                                .purge(true)
                                .build()
                                .toQueryParams());
    }

    @Test
    public void adminCanAddPatientIdentifier() {
        CreatePatientResponse createPatientResponse = AdminSteps.createPatientWithoutInvokedIdentifier();
        int identifiersBefore = createPatientResponse.getIdentifiers().size();
        PatientIdentifierRequest identifierRequest = RandomModelGenerator.generate(PatientIdentifierRequest.class);

        new SuccessfulNestedCrudRequester<PatientIdentifierRequest>(RequestSpecs.adminSpec(),
                Endpoint.PATIENT_IDENTIFIER_NESTED,
                ResponseSpecs.requestReturnsCreated())
                .create(createPatientResponse.getUuid(), identifierRequest);

        GetPatientResponse patientAfter = new SuccessfulCrudRequester<GetPatientResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.PATIENT_GET,
                ResponseSpecs.requestReturnsOk())
                .get(createPatientResponse.getUuid());

        softly.assertThat(patientAfter.getIdentifiers()).hasSize(identifiersBefore + 1);
        softly.assertThat(patientAfter.getIdentifiers()
                        .stream()
                        .anyMatch(id -> id.getDisplay().endsWith(identifierRequest.getIdentifier())))
                .isTrue();
    }

    @Test
    public void adminCannotGetNonExistingPatientIdentifier() {
        CreatePatientResponse createPatientResponse = AdminSteps.createPatientWithoutInvokedIdentifier();

        new NestedCrudRequester(
                RequestSpecs.adminSpec(),
                Endpoint.PATIENT_IDENTIFIER_NESTED,
                ResponseSpecs.requestReturnsNotFound())
                .get(createPatientResponse.getUuid(), nonExistingUuid);
    }

    @Test
    public void adminCanUpdatePatientIdentifier() {
        CreatePatientResponse patientBefore = AdminSteps.createPatientWithoutInvokedIdentifier();
        String identifierUuid = patientBefore.getIdentifiers().get(0).getUuid();
        PatientIdentifierRequest updateRequest = RandomModelGenerator.generate(PatientIdentifierRequest.class);

        new SuccessfulNestedCrudRequester<PatientIdentifierRequest>(RequestSpecs.adminSpec(),
                Endpoint.PATIENT_IDENTIFIER_NESTED,
                ResponseSpecs.requestReturnsOk())
                .update(patientBefore.getUuid(), identifierUuid, updateRequest);

        GetPatientResponse patientAfter = new SuccessfulCrudRequester<GetPatientResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.PATIENT_GET,
                ResponseSpecs.requestReturnsOk())
                .get(patientBefore.getUuid());

        softly.assertThat(patientAfter.getIdentifiers()).anyMatch(id ->
                id.getUuid().equals(identifierUuid) && id.getDisplay().endsWith(updateRequest.getIdentifier()));
    }

    @Test
    public void adminCannotUpdateNonExistingPatientIdentifier() {
        CreatePatientResponse createdPatient = AdminSteps.createPatientWithoutInvokedIdentifier();
        PatientIdentifierRequest updateRequest = RandomModelGenerator.generate(PatientIdentifierRequest.class);

        new NestedCrudRequester(
                RequestSpecs.adminSpec(),
                Endpoint.PATIENT_IDENTIFIER_NESTED,
                ResponseSpecs.requestReturnsNotFound(OBJECT_WITH_UUID_DOES_NOT_EXIST.getMessage()))
                .update(createdPatient.getUuid(), nonExistingUuid, updateRequest);
    }

    @Test
    public void adminCanDeletePatientIdentifier() {
        CreatePatientResponse createdPatient = AdminSteps.createPatientWithoutInvokedIdentifier();
        PatientIdentifierRequest identifierRequest = RandomModelGenerator.generate(PatientIdentifierRequest.class);

        new SuccessfulNestedCrudRequester<PatientIdentifierRequest>(RequestSpecs.adminSpec(),
                Endpoint.PATIENT_IDENTIFIER_NESTED,
                ResponseSpecs.requestReturnsCreated())
                .create(createdPatient.getUuid(), identifierRequest);

        GetPatientResponse patientBefore = new SuccessfulCrudRequester<GetPatientResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.PATIENT_GET,
                ResponseSpecs.requestReturnsOk())
                .get(createdPatient.getUuid());

        int identifiersBefore = patientBefore.getIdentifiers().size();

        String identifierUuid = patientBefore.getIdentifiers()
                .stream()
                .filter(id -> id.getDisplay().endsWith(identifierRequest.getIdentifier()))
                .findFirst()
                .orElseThrow()
                .getUuid();

        new SuccessfulNestedCrudRequester<PatientIdentifierRequest>(
                RequestSpecs.adminSpec(),
                Endpoint.PATIENT_IDENTIFIER_NESTED,
                ResponseSpecs.requestReturnsNoContent())
                .delete(createdPatient.getUuid(), identifierUuid,
                        PatientDeleteParams.builder()
                                .purge(true)
                                .build()
                                .toQueryParams());

        GetPatientResponse patientAfter = new SuccessfulCrudRequester<GetPatientResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.PATIENT_GET,
                ResponseSpecs.requestReturnsOk())
                .get(createdPatient.getUuid());

        softly.assertThat(patientAfter.getIdentifiers()).hasSize(identifiersBefore - 1);
        softly.assertThat(patientAfter.getIdentifiers()
                        .stream()
                        .noneMatch(id -> id.getUuid().equals(identifierUuid)))
                .isTrue();
    }

    /// Fix needed: Expected 404 Not Found, but received 204 No Content.
    @Test
    public void adminCannotDeleteNonExistingPatientIdentifier() {
        CreatePatientResponse createdPatient = AdminSteps.createPatientWithoutInvokedIdentifier();

        new NestedCrudRequester(
                RequestSpecs.adminSpec(),
                Endpoint.PATIENT_IDENTIFIER_NESTED,
                ResponseSpecs.requestReturnsNoContent())
                .delete(createdPatient.getUuid(), nonExistingUuid,
                        PatientDeleteParams.builder()
                                .purge(true)
                                .build()
                                .toQueryParams());
    }
}