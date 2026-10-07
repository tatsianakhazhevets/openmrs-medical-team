package uiTests;

import apiParts.generators.RandomNameGenerator;
import apiParts.specs.RequestSpecs;
import com.codeborne.selenide.Condition;
import org.junit.jupiter.api.Test;
import uiParts.pages.BasePage;
import uiParts.pages.HomeServiceQueues;
import uiParts.pages.PatientChartPage;
import uiParts.pages.PatientRegistrationPage;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class PatientManagementUiTests extends BaseUiTest{

    @Test
    public void userCanCreatePatientWithRequiredFieldsTest() {
        String patientGivenName = RandomNameGenerator.generateNonExistingName();
        String patientFamilyName = RandomNameGenerator.generateNonExistingName();
        String dateOfBirth = "12/12/1995";
       BasePage.authAsUser(RequestSpecs.ADMIN_USERNAME, RequestSpecs.ADMIN_PASSWORD);

        String createdPatientUuid = new PatientRegistrationPage()
                .open()
                .fillRequiredPatientFields(patientGivenName, patientFamilyName, dateOfBirth)
                .submitRegistration()
                .getPage(PatientChartPage.class)
                .getCreatedPatientUuid();

        assertThat(createdPatientUuid).isNotBlank();
        assertThat(createdPatientUuid).hasSize(36);

        PatientChartPage chartPage = new PatientChartPage();
        chartPage.getPatientChartValidationElement().shouldBe(Condition.visible);
    }

    @Test
    public void userCannotCreatePatientWithoutFamilyNameTest() {
        String patientGivenName = RandomNameGenerator.generateNonExistingName();
        String emptyFamilyName = "";
        String dateOfBirth = "12/12/1995";

        BasePage.authAsUser(RequestSpecs.ADMIN_USERNAME, RequestSpecs.ADMIN_PASSWORD);

        String actualErrorText = new PatientRegistrationPage()
                .open()
                .fillRequiredPatientFields(patientGivenName, emptyFamilyName, dateOfBirth)
                .submitRegistration()
                .getFamilyNameValidationText();

        assertThat(actualErrorText).contains("required");
        new PatientRegistrationPage().getRegisterPatientButton().shouldBe(Condition.visible);
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