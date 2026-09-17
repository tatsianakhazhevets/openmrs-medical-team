package apiTests;

import apiParts.models.Location;
import apiParts.models.patient.*;
import apiParts.skelethon.endpoints.Endpoint;
import apiParts.skelethon.requests.common.CrudRequester;
import apiParts.skelethon.requests.common.SuccessfulCrudRequester;
import apiParts.skelethon.requests.identifier.IdentifierRequester;
import apiParts.skelethon.requests.patient.PatientRequester;
import apiParts.specs.RequestSpecs;
import apiParts.specs.ResponseSpecs;
import apiParts.steps.AdminSteps;
import net.datafaker.Faker;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import static apiParts.steps.AdminSteps.getPatientIdentifier;
import static apiParts.models.errors.PatientErrorMessages.*;

public class PatientManagementApiTests extends BaseTest {

    Faker faker = new Faker(new Locale("en", "US"));

    String gender = faker.gender().binaryTypes();
    String shortGender = gender.equals("Male") ? "M" : "F";
    String givenName = faker.name().firstName();
    String familyName = faker.name().lastName();
    String birthdate = faker.timeAndDate().birthday(18, 65, "yyyy-MM-dd");
    String city = faker.address().city();
    String postalCode = faker.address().postcode();
    String address = faker.address().streetAddress();
    String country = StringUtils.left(faker.address().country(), 50);
    String identifier = AdminSteps.getPatientIdentifier();
    String nonExistingUuid = "00000000-0000-0000-0000-000000000000";
    Map<String, Boolean> queryParam = Map.of("purge", true);

    @Test
    public void adminCanCreatePatient() {
        CreatePatientRequest createPatientRequest = CreatePatientRequest.builder()
                .person(PersonRequest.builder()
                        .gender(shortGender)
                        .birthdate(birthdate)
                        .birthdateEstimated(false)
                        .dead(false)
                        .names(List.of(
                                PersonName.builder()
                                        .givenName(givenName)
                                        .familyName(familyName)
                                        .build()))
                        .addresses(List.of(
                                PersonAddress.builder()
                                        .address1(address)
                                        .cityVillage(city)
                                        .country(country)
                                        .postalCode(postalCode)
                                        .build()))
                        .build())
                .identifiers(List.of(
                        PatientIdentifierRequest.builder()
                                .identifier(identifier)
                                .identifierType(IdentifierType.TYPE.getType())
                                .location(Location.OUTPATIENT_CLINIC.getUuid())
                                .preferred(true)
                                .build()))
                .build();

        CreatePatientResponse createdPatientResponse = new SuccessfulCrudRequester<CreatePatientResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.PATIENT_POST,
                ResponseSpecs.requestReturnsCreated())
                .create(createPatientRequest);

        softly.assertThat(createdPatientResponse.getUuid()).isNotNull();

        GetPatientResponse getPatientResponse = new SuccessfulCrudRequester<GetPatientResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.PATIENT_GET,
                ResponseSpecs.requestReturnsOk())
                .get(createdPatientResponse.getUuid());

        softly.assertThat(getPatientResponse.getUuid()).isEqualTo(createdPatientResponse.getUuid());
        softly.assertThat(getPatientResponse.getPerson()).isNotNull();
        softly.assertThat(getPatientResponse.getPerson().getUuid()).isEqualTo(createdPatientResponse.getUuid());
        softly.assertThat(getPatientResponse.getPerson().getGender()).isEqualTo(shortGender);
        softly.assertThat(getPatientResponse.getPerson().getBirthdate()).startsWith(birthdate);
        softly.assertThat(getPatientResponse.getPerson().getBirthdateEstimated()).isFalse();
        softly.assertThat(getPatientResponse.getPerson().getDead()).isFalse();
        softly.assertThat(getPatientResponse.getPerson().getPreferredName()).isNotNull();
        softly.assertThat(getPatientResponse.getPerson().getPreferredName().getDisplay()).isEqualTo(givenName + " " + familyName);
        softly.assertThat(getPatientResponse.getPerson().getPreferredAddress()).isNotNull();
        softly.assertThat(getPatientResponse.getPerson().getPreferredAddress().getDisplay()).isEqualTo(address);
        softly.assertThat(getPatientResponse.getIdentifiers()).isNotEmpty();
        softly.assertThat(getPatientResponse.getIdentifiers().get(0).getDisplay()).endsWith(identifier);
    }

    @Test
    public void adminCannotCreatePatientWithoutPerson() {
        CreatePatientRequest createPatientRequest =
                CreatePatientRequest.builder()
                        .identifiers(List.of(
                                PatientIdentifierRequest.builder()
                                        .identifier(identifier)
                                        .identifierType(IdentifierType.TYPE.getType())
                                        .location(Location.OUTPATIENT_CLINIC.getUuid())
                                        .preferred(true)
                                        .build()))
                        .build();

        new CrudRequester(
                RequestSpecs.adminSpec(),
                Endpoint.PATIENT_POST,
                ResponseSpecs.requestReturnsBadRequestWithMessage(
                        PERSON_IS_MISSING.getMessage()))
                .create(createPatientRequest);
    }

    @Test
    public void adminCannotCreatePatientWithoutIdentifiers() {

        CreatePatientRequest createPatientRequest =
                CreatePatientRequest.builder()
                        .person(PersonRequest.builder()
                                .gender(gender)
                                .birthdate(birthdate)
                                .birthdateEstimated(false)
                                .dead(false)
                                .names(List.of(
                                        PersonName.builder()
                                                .givenName(givenName)
                                                .familyName(familyName)
                                                .build()))
                                .build())
                        .build();

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
        CreatePatientResponse createPatientResponse = AdminSteps.createPatient();
        String searchQuery = createPatientResponse.getPerson().getPreferredName().getDisplay();
        Map<String, Object> queryParams = Map.of(
                "q", searchQuery,
                "v", "default",
                "limit", 10);

        PatientSearchResponse patientSearchResponse = new SuccessfulCrudRequester<PatientSearchResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.PATIENT_SEARCH_GET,
                ResponseSpecs.requestReturnsOk())
                .get(queryParams);

        softly.assertThat(patientSearchResponse.getResults()).isNotEmpty();
        softly.assertThat(patientSearchResponse.getResults())
                .anyMatch(patient -> patient.getUuid().equals(createPatientResponse.getUuid()));
    }

    /// Fix needed: Expected status code <404> but was <200>
    @Test
    public void adminCannotFindNonExistingPatient() {
        String searchQuery = "non-existing-patient-" + System.currentTimeMillis();
        Map<String, Object> queryParams = Map.of(
                "q", searchQuery,
                "v", "default",
                "limit", 10);

        PatientSearchResponse searchResponse = new SuccessfulCrudRequester<PatientSearchResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.PATIENT_SEARCH_GET,
                ResponseSpecs.requestReturnsOk())
                .get(queryParams);

        softly.assertThat(searchResponse.getResults()).isEmpty();
    }

    @Test
    public void adminCanUpdatePatient() {
        CreatePatientResponse createPatientResponse = AdminSteps.createPatient();

        String updatedGivenName = faker.name().firstName();
        String updatedFamilyName = faker.name().lastName();

        CreatePatientRequest updatePatientRequest =
                CreatePatientRequest.builder()
                        .person(PersonRequest.builder()
                                .names(List.of(
                                        PersonName.builder()
                                                .givenName(updatedGivenName)
                                                .familyName(updatedFamilyName)
                                                .build()))
                                .build())
                        .build();

        new PatientRequester(
                RequestSpecs.adminSpec(),
                Endpoint.PATIENT_POST,
                ResponseSpecs.requestReturnsOk())
                .update(createPatientResponse.getUuid(), updatePatientRequest);

        GetPatientResponse getPatientResponse = new SuccessfulCrudRequester<GetPatientResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.PATIENT_GET,
                ResponseSpecs.requestReturnsOk())
                .get(createPatientResponse.getUuid());

        softly.assertThat(getPatientResponse.getPerson()).isNotNull();
        softly.assertThat(getPatientResponse.getPerson().getPreferredName()).isNotNull();
        softly.assertThat(
                        getPatientResponse.getPerson()
                                .getPreferredName()
                                .getDisplay())
                .isEqualTo(updatedGivenName + " " + updatedFamilyName);
    }

    /// Fix needed: Expected status code <404> but was <500>.
    @Test
    public void adminCannotUpdateNonExistingPatient() {
        String updatedGivenName = faker.name().firstName();

        CreatePatientRequest updatePatientRequest =
                CreatePatientRequest.builder()
                        .person(PersonRequest.builder()
                                .names(List.of(
                                        PersonName.builder()
                                                .givenName(updatedGivenName)
                                                .build()))
                                .build())
                        .build();

        new PatientRequester(
                RequestSpecs.adminSpec(),
                Endpoint.PATIENT_POST,
                ResponseSpecs.requestReturnsServerError())
                .update(nonExistingUuid, updatePatientRequest);
    }

    @Test
    public void adminCanDeletePatient() {
        CreatePatientResponse createPatientResponse = AdminSteps.createPatient();

        softly.assertThat(createPatientResponse.getUuid())
                .isNotNull();

        GetPatientResponse patientBeforeDelete = new SuccessfulCrudRequester<GetPatientResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.PATIENT_GET,
                ResponseSpecs.requestReturnsOk())
                .get(createPatientResponse.getUuid());

        softly.assertThat(patientBeforeDelete.getUuid()).isEqualTo(createPatientResponse.getUuid());

        new PatientRequester(
                RequestSpecs.adminSpec(),
                Endpoint.PATIENT_DELETE,
                ResponseSpecs.requestReturnsNoContent())
                .delete(createPatientResponse.getUuid(), queryParam);

        new CrudRequester(
                RequestSpecs.adminSpec(),
                Endpoint.PATIENT_GET,
                ResponseSpecs.requestReturnsNotFound())
                .get(createPatientResponse.getUuid());
    }

    /// Fix needed: Expected status code <404> but was <204>.
    @Test
    public void adminCannotDeleteNonExistingPatient() {
        new PatientRequester(
                RequestSpecs.adminSpec(),
                Endpoint.PATIENT_DELETE,
                ResponseSpecs.requestReturnsNoContent())
                .delete(nonExistingUuid, queryParam);
    }

    @Test
    public void adminCanAddPatientIdentifier() {
        CreatePatientResponse createPatientResponse = AdminSteps.createPatient();

        GetPatientResponse patientBefore =
                new SuccessfulCrudRequester<GetPatientResponse>(
                        RequestSpecs.adminSpec(),
                        Endpoint.PATIENT_GET,
                        ResponseSpecs.requestReturnsOk())
                        .get(createPatientResponse.getUuid());

        int identifiersBefore = patientBefore.getIdentifiers().size();
        //String identifier = getPatientIdentifier();

        PatientIdentifierRequest identifierRequest =
                PatientIdentifierRequest.builder()
                        .identifier(identifier)
                        .identifierType(IdentifierType.TYPE.getType())
                        .location(Location.OUTPATIENT_CLINIC.getUuid())
                        .preferred(false)
                        .build();

        new PatientRequester(
                RequestSpecs.adminSpec(),
                Endpoint.PATIENT_IDENTIFIER_POST,
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
                        .anyMatch(id -> id.getDisplay().endsWith(identifier)))
                .isTrue();
    }

    /// Fix needed: Expected status code <404> but was <200>.
    @Test
    public void adminCannotGetNonExistingPatientIdentifier() {
        CreatePatientResponse createPatientResponse = AdminSteps.createPatient();

        new IdentifierRequester(
                RequestSpecs.adminSpec(),
                Endpoint.IDENTIFIER_GET,
                ResponseSpecs.requestReturnsOk())
                .get(createPatientResponse.getUuid(), nonExistingUuid);
    }

    @Test
    public void adminCanUpdatePatientIdentifier() {
        CreatePatientResponse createdPatient = AdminSteps.createPatient();

        GetPatientResponse patientBefore =
                new SuccessfulCrudRequester<GetPatientResponse>(
                        RequestSpecs.adminSpec(),
                        Endpoint.PATIENT_GET,
                        ResponseSpecs.requestReturnsOk())
                        .get(createdPatient.getUuid());

        String identifierUuid = patientBefore.getIdentifiers().get(0).getUuid();
        String updatedIdentifier = getPatientIdentifier();

        PatientIdentifierRequest updateRequest =
                PatientIdentifierRequest.builder()
                        .identifier(updatedIdentifier)
                        .identifierType(IdentifierType.TYPE.getType())
                        .location(Location.OUTPATIENT_CLINIC.getUuid())
                        .preferred(false)
                        .build();

        new IdentifierRequester(
                RequestSpecs.adminSpec(),
                Endpoint.PATIENT_IDENTIFIER_UPDATE,
                ResponseSpecs.requestReturnsOk())
                .update(createdPatient.getUuid(), identifierUuid, updateRequest);

        GetPatientResponse patientAfter = new SuccessfulCrudRequester<GetPatientResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.PATIENT_GET,
                ResponseSpecs.requestReturnsOk())
                .get(createdPatient.getUuid());

        softly.assertThat(
                        patientAfter.getIdentifiers()
                                .stream()
                                .anyMatch(
                                        id -> id.getUuid().equals(identifierUuid) && id.getDisplay().endsWith(updatedIdentifier)))
                .isTrue();
    }

    @Test
    public void adminCannotUpdateNonExistingPatientIdentifier() {
        CreatePatientResponse createdPatient = AdminSteps.createPatient();

        PatientIdentifierRequest updateRequest =
                PatientIdentifierRequest.builder()
                        .identifier(
                                getPatientIdentifier())
                        .build();

        new IdentifierRequester(
                RequestSpecs.adminSpec(),
                Endpoint.PATIENT_IDENTIFIER_UPDATE,
                ResponseSpecs.requestReturnsNotFound(OBJECT_WITH_UUID_DOES_NOT_EXIST.getMessage()))
                .update(createdPatient.getUuid(), nonExistingUuid, updateRequest);
    }

    @Test
    public void adminCanDeletePatientIdentifier() {
        CreatePatientResponse createdPatient = AdminSteps.createPatient();

        PatientIdentifierRequest identifierRequest =
                PatientIdentifierRequest.builder()
                        .identifier(identifier)
                        .identifierType(IdentifierType.TYPE.getType())
                        .location(Location.OUTPATIENT_CLINIC.getUuid())
                        .preferred(false)
                        .build();

        new PatientRequester(
                RequestSpecs.adminSpec(),
                Endpoint.PATIENT_IDENTIFIER_POST,
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
                .filter(id ->
                        id.getDisplay()
                                .endsWith(identifier))
                .findFirst()
                .orElseThrow()
                .getUuid();

        new IdentifierRequester(
                RequestSpecs.adminSpec(),
                Endpoint.PATIENT_IDENTIFIER_DELETE,
                ResponseSpecs.requestReturnsNoContent())
                .delete(createdPatient.getUuid(), identifierUuid, queryParam);

        GetPatientResponse patientAfter = new SuccessfulCrudRequester<GetPatientResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.PATIENT_GET,
                ResponseSpecs.requestReturnsOk())
                .get(createdPatient.getUuid());

        softly.assertThat(patientAfter.getIdentifiers()).hasSize(identifiersBefore - 1);
        softly.assertThat(patientAfter.getIdentifiers()
                        .stream()
                        .noneMatch(id ->
                                id.getUuid().equals(identifierUuid)))
                .isTrue();
    }

    /// Fix needed: as Expected status code <404> but was <204>.
    @Test
    public void adminCannotDeleteNonExistingPatientIdentifier() {
        CreatePatientResponse createdPatient = AdminSteps.createPatient();

        new IdentifierRequester(
                RequestSpecs.adminSpec(),
                Endpoint.PATIENT_IDENTIFIER_DELETE,
                ResponseSpecs.requestReturnsNoContent())
                .delete(createdPatient.getUuid(), nonExistingUuid, queryParam);
    }
}