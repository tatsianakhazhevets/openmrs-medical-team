package uiParts.pages;

import com.codeborne.selenide.SelenideElement;
import com.codeborne.selenide.WebDriverRunner;

import static com.codeborne.selenide.Selenide.$;

public class PatientChartPage extends BasePage<PatientChartPage> {
    private final SelenideElement patientIdentifier = $("[data-testid='patient-identifier'], .cds--form-item p");

    @Override
    public String url() {
        return "/patient";
    }

    public String getCreatedPatientUuid() {
        patientIdentifier.shouldBe(com.codeborne.selenide.Condition.visible);
        String currentUrl = WebDriverRunner.url(); // http://localhost/openmrs/spa/patient/44c3ef-b025.../chart
        return currentUrl.replaceAll(".*/patient/([^/]+).*", "$1");
    }
}