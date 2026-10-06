package uiTests;

import apiParts.generators.RandomNameGenerator;
import apiParts.specs.RequestSpecs;
import com.codeborne.selenide.Condition;
import org.junit.jupiter.api.Test;
import uiParts.pages.BasePage;
import uiParts.pages.PatientChartPage;
import uiParts.pages.PatientRegistrationPage;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class PatientManagementUiTests extends BaseUiTest{
    @Test
    public void userCanCreatePatientWithRequiredFieldsTest() {
        String patientGivenName = RandomNameGenerator.generateNonExistingName();
        String patientFamilyName = RandomNameGenerator.generateNonExistingName();
        String dateOfBirth = "12/12/1995";

        // Быстрая авторизация через API-куки, чтобы UI сразу открыл нужный роут
        //BasePage.authAsUser(RequestSpecs.ADMIN_USERNAME, RequestSpecs.ADMIN_PASSWORD);

        String createdPatientUuid = new PatientRegistrationPage()
                .open()
                .fillRequiredPatientFields(patientGivenName, patientFamilyName, dateOfBirth)
                .submitRegistration()
                .getPage(PatientChartPage.class)
                .getCreatedPatientUuid();

        assertThat(createdPatientUuid).isNotBlank();

        //PatientChartPage chartPage = new PatientChartPage();
        //chartPage.getPatientIdentifier().shouldBe(Condition.visible);

        /*var backendPatientResponse = new PatientStep().getPatientByUuid(createdPatientUuid);
        assertThat(backendPatientResponse.getGivenName()).isEqualTo(patientGivenName);
        assertThat(backendPatientResponse.getFamilyName()).isEqualTo(patientFamilyName);*/
    }
}
