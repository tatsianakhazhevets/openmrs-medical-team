package uiTests;

import apiParts.generators.RandomDataGenerator;
import apiParts.generators.RandomNameGenerator;
import apiParts.models.patient.GetPatientResponse;
import apiParts.models.search.SearchResult;
import apiParts.specs.RequestSpecs;
import apiParts.steps.PatientSteps;
import org.junit.jupiter.api.Test;
import uiParts.pages.BasePage;
import uiParts.pages.HomeServiceQueues;
import uiParts.pages.PatientChartPage;
import uiParts.pages.PatientRegistrationPage;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static uiParts.errorMessages.PatientUiErrorMessages.*;
import static uiParts.pages.PatientChartPage.PATIENT_URL;

public class PatientManagementUiTests extends BaseUiTest {

    @Test
    public void userCanCreatePatientWithRequiredFieldsTest() {
        //1. Test data
        String patientGivenName = RandomNameGenerator.generateNonExistingName();
        String patientFamilyName = RandomNameGenerator.generateNonExistingName();
        String dateOfBirth = RandomDataGenerator.generateBirthDate();
        BasePage.authAsUser(RequestSpecs.ADMIN_USERNAME, RequestSpecs.ADMIN_PASSWORD);

        //2. Test steps
        PatientChartPage createdPatient = new PatientRegistrationPage()
                .open()
                .fillRequiredPatientFields(patientGivenName, patientFamilyName, dateOfBirth)
                .submitRegistration()
                .getPage(PatientChartPage.class)
                .getPatientChartValidationElement();

        //3. Test result
        String patientUuid = createdPatient.getCreatedPatientUuid();
        assertThat(patientUuid).isNotBlank();
        assertThat(patientUuid).hasSize(36);

        GetPatientResponse getPatientResponse = PatientSteps.getPatientPositive(patientUuid);
        assertThat(getPatientResponse.getPerson().getDisplay()).contains(patientGivenName);
        assertThat(getPatientResponse.getPerson().getDisplay()).contains(patientFamilyName);
    }

    @Test
    public void userCannotCreatePatientWithoutFamilyNameTest() {
        //1. Test data
        String patientGivenName = RandomNameGenerator.generateNonExistingName();
        String emptyFamilyName = "";
        String dateOfBirth = RandomDataGenerator.generateBirthDate();
        BasePage.authAsUser(RequestSpecs.ADMIN_USERNAME, RequestSpecs.ADMIN_PASSWORD);

        //2. Test steps
        PatientRegistrationPage createdPatient = new PatientRegistrationPage()
                .open()
                .fillRequiredPatientFields(patientGivenName, emptyFamilyName, dateOfBirth)
                .submitRegistration()
                .verifyFamilyNameValidationErrorVisible()
                .verifyRegisterButtonIsVisible();

        //3. Test result
        String actualErrorText = createdPatient.getFamilyNameValidationText();
        assertThat(actualErrorText).contains(FAMILY_NAME_IS_REQUIRED.getMessage());

        SearchResult<GetPatientResponse> searchResult = PatientSteps.getPatients(patientGivenName);
        assertThat(searchResult.isEmpty()).isTrue();
    }

    @Test
    public void userCannotCreatePatientWithoutGivenNameTest() {
        //1. Test data
        String emptyGivenName = "";
        String patientFamilyName = RandomNameGenerator.generateNonExistingName();
        String dateOfBirth = RandomDataGenerator.generateBirthDate();
        BasePage.authAsUser(RequestSpecs.ADMIN_USERNAME, RequestSpecs.ADMIN_PASSWORD);

        //2. Test steps
        PatientRegistrationPage createdPatient = new PatientRegistrationPage()
                .open()
                .fillRequiredPatientFields(emptyGivenName, patientFamilyName, dateOfBirth)
                .submitRegistration()
                .verifyGivenNameValidationErrorVisible()
                .verifyRegisterButtonIsVisible();

        //3. Test result
        String actualErrorText = createdPatient.getGivenNameValidationText();
        assertThat(actualErrorText).contains(GIVEN_NAME_IS_REQUIRED.getMessage());

        SearchResult<GetPatientResponse> searchResult = PatientSteps.getPatients(patientFamilyName);
        assertThat(searchResult.isEmpty()).isTrue();
    }

    @Test
    public void userCannotCreatePatientWithoutBirthDateTest() {
        //1. Test data
        String patientGivenName = RandomNameGenerator.generateNonExistingName();
        String patientFamilyName = RandomNameGenerator.generateNonExistingName();
        String emptyBirthDate = "";
        BasePage.authAsUser(RequestSpecs.ADMIN_USERNAME, RequestSpecs.ADMIN_PASSWORD);

        //2. Test steps
        PatientRegistrationPage createdPatient = new PatientRegistrationPage()
                .open()
                .fillRequiredPatientFields(patientGivenName, patientFamilyName, emptyBirthDate)
                .submitRegistration()
                .verifyBirthDateValidationErrorVisible()
                .verifyRegisterButtonIsVisible();

        //3. Test result
        String actualErrorText = createdPatient.getBirthDateValidationText();
        assertThat(actualErrorText).isEqualTo(BIRTHDAY_IS_REQUIRED.getMessage());

        SearchResult<GetPatientResponse> searchResult = PatientSteps.getPatients(patientGivenName);
        assertThat(searchResult.isEmpty()).isTrue();
    }

    @Test
    public void userCanFindCreatedPatientByNameTest() {
        //1. Test data
        String patientGivenName = RandomNameGenerator.generateNonExistingName();
        String patientFamilyName = RandomNameGenerator.generateNonExistingName();
        String dateOfBirth = RandomDataGenerator.generateBirthDate();
        BasePage.authAsUser(RequestSpecs.ADMIN_USERNAME, RequestSpecs.ADMIN_PASSWORD);

        //2. Test steps
        PatientChartPage foundPatientPage = new PatientRegistrationPage()
                .open()
                .fillRequiredPatientFields(patientGivenName, patientFamilyName, dateOfBirth)
                .submitRegistration()
                .getPage(PatientChartPage.class)
                .getPage(HomeServiceQueues.class)
                .open()
                .searchPatient(patientGivenName)
                .clickCreatedPatient(patientGivenName, patientFamilyName)
                .getPage(PatientChartPage.class);

        //3. Test result
        String finalUrl = foundPatientPage.getPageUrlAfterSubmit();

        assertThat(finalUrl).contains(PATIENT_URL);
        String patientUuid = foundPatientPage.getCreatedPatientUuid();
        assertThat(patientUuid).isNotBlank().hasSize(36);

        GetPatientResponse apiPatient = PatientSteps.getPatientPositive(patientUuid);
        assertThat(apiPatient.getPerson().getDisplay()).contains(patientGivenName);
        assertThat(apiPatient.getPerson().getDisplay()).contains(patientFamilyName);
    }

    @Test
    public void userCannotFindNotCreatedPatientByNameTest() {
        //1. Test data
        String patientGivenName = RandomNameGenerator.generateNonExistingName();
        String patientFamilyName = RandomNameGenerator.generateNonExistingName();
        BasePage.authAsUser(RequestSpecs.ADMIN_USERNAME, RequestSpecs.ADMIN_PASSWORD);

        //2. Test steps
        PatientChartPage foundPatientPage = new HomeServiceQueues()
                .open()
                .searchPatient(patientGivenName)
                .clickCreatedPatient(patientGivenName, patientFamilyName)
                .getPage(PatientChartPage.class);

        //3. Test result
        String finalUrl = foundPatientPage.getPageUrlAfterSubmit();

        assertThat(finalUrl).doesNotContain(PATIENT_URL);
        String patientUuid = foundPatientPage.getCreatedPatientUuid();
        assertThat(patientUuid).isBlank();

        GetPatientResponse apiPatient = PatientSteps.getPatientPositive(patientUuid);
        assertThat(apiPatient.getPerson().getDisplay()).doesNotContain(patientGivenName);
        assertThat(apiPatient.getPerson().getDisplay()).doesNotContain(patientFamilyName);
    }
}