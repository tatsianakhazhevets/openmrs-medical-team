package uiParts.pages;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;
import com.codeborne.selenide.WebDriverRunner;
import lombok.Getter;

import java.time.Duration;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$x;

@Getter
public class PatientChartPage extends BasePage<PatientChartPage> {
    private final SelenideElement patientChartValidationElement =
            $x("//button[normalize-space()='Actions'] | //button[contains(text(), 'visit')]");

    @Override
    public String url() {
        return "/patient";
    }

    public String getCreatedPatientUuid() {
        patientChartValidationElement.shouldBe(Condition.visible, Duration.ofSeconds(15));
        String currentUrl = WebDriverRunner.url();
        return currentUrl.replaceAll(".*/patient/([^/]+).*", "$1");
    }

    public String getPageUrlAfterSubmit() {
        // Ждем, пока медицинская карта визуально загрузится, прежде чем брать URL
        patientChartValidationElement.shouldBe(Condition.visible, Duration.ofSeconds(15));
        return WebDriverRunner.url();
    }
}