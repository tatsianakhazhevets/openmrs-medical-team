package uiTests;

import apiParts.generators.RandomDataGenerator;
import apiParts.generators.RandomNameGenerator;
import apiParts.models.patient.GetPatientResponse;
import apiParts.models.search.SearchResult;
import apiParts.specs.RequestSpecs;
import apiParts.steps.PatientSteps;
import com.codeborne.selenide.Condition;
import org.junit.jupiter.api.Test;
import uiParts.pages.BasePage;
import uiParts.pages.HomeServiceQueues;
import uiParts.pages.PatientChartPage;
import uiParts.pages.PatientRegistrationPage;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static uiParts.errorMessages.PatientUiErrorMessages.*;

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
                .verifyFamilyNameValidationErrorVisible();

        //3. Test result
        String actualErrorText = createdPatient.getFamilyNameValidationText();
        assertThat(actualErrorText).contains(FAMILY_NAME_IS_REQUIRED.getMessage());

        SearchResult<GetPatientResponse> searchResult = PatientSteps.getPatients(patientGivenName);
        assertThat(searchResult.isEmpty()).isTrue();
    }

    @Test
    public void userCannotCreatePatientWithoutBirthDateTest() {
        String patientGivenName = RandomNameGenerator.generateNonExistingName();
        String patientFamilyName = RandomNameGenerator.generateNonExistingName();
        String emptyBirthDate = "";

        BasePage.authAsUser(RequestSpecs.ADMIN_USERNAME, RequestSpecs.ADMIN_PASSWORD);

        String actualErrorText = new PatientRegistrationPage()
                .open()
                .fillRequiredPatientFields(patientGivenName, patientFamilyName, emptyBirthDate)
                .submitRegistration()
                .getBirthDateValidationText();

        assertThat(actualErrorText).isEqualTo("Birthday is required");
        new PatientRegistrationPage().getRegisterPatientButton().shouldBe(Condition.visible);
    }

    @Test
    public void userCanFindCreatedPatientByNameTest() {
        String patientGivenName = RandomNameGenerator.generateNonExistingName();
        String patientFamilyName = RandomNameGenerator.generateNonExistingName();
        String dateOfBirth = "12/12/1995";

        BasePage.authAsUser(RequestSpecs.ADMIN_USERNAME, RequestSpecs.ADMIN_PASSWORD);

        new PatientRegistrationPage()
                .open()
                .fillRequiredPatientFields(patientGivenName, patientFamilyName, dateOfBirth)
                .submitRegistration()
                .getPage(PatientChartPage.class)
                .getCreatedPatientUuid();

        String finalUrl = new HomeServiceQueues()
                .open()
                .searchPatient(patientGivenName)
                .clickCreatedPatient(patientGivenName, patientFamilyName)
                .getPage(PatientChartPage.class)
                .getPageUrlAfterSubmit();

        assertThat(finalUrl).contains("/patient/");
    }
}