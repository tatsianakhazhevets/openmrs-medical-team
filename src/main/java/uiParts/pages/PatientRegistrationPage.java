package uiParts.pages;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;
import lombok.Getter;

import java.time.Duration;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$x;

@Getter
public class PatientRegistrationPage extends BasePage<PatientRegistrationPage> {
    private final SelenideElement givenNameInput = $("input[name='givenName']");
    private final SelenideElement familyNameInput = $("input[name='familyName']");
    private final SelenideElement genderMaleLabel = $x("//label[normalize-space()='Male']");
    private final SelenideElement birthDateInput = $("input[type='date'], input[placeholder='mm/dd/yyyy']");
    private final SelenideElement registerPatientButton = $x("//button[normalize-space()='Register patient']");
    private final SelenideElement familyNameError = $("#familyName-error-msg");
    private final SelenideElement familyNameLabel = $x("//label[@for='familyName']");
    private final SelenideElement birthDateLabel = $x("//label[contains(@for, 'birthdate')]");
    private final SelenideElement birthSectionHeader = $x("//h4[normalize-space()='Birth']");
    private final SelenideElement birthDateError = $("span[slot='errorMessage']");
    private final SelenideElement birthDateWrapper = $(".cds--date-picker, [class*='dobField'], input[name='birthdate'], input[type='date']");

    @Override
    public String url() {
        return "/patient-registration";
    }

    public PatientRegistrationPage fillRequiredPatientFields(String givenName, String familyName, String dob) {
        setValue(givenNameInput, givenName);
        setValue(familyNameInput, familyName);
        click(genderMaleLabel);
        setValue(birthDateInput, dob);
        return this;
    }

    public PatientRegistrationPage submitRegistration() {
        click(registerPatientButton);
        return this;
    }

    public String getFamilyNameValidationText() {
        familyNameLabel.click();
        return familyNameError.shouldBe(Condition.visible, Duration.ofSeconds(8)).getText();
    }

    public String getBirthDateValidationText() {
        birthSectionHeader.click(); // Снимаем фокус
        return birthDateError.shouldBe(Condition.visible, Duration.ofSeconds(8)).getText();
    }

    public PatientRegistrationPage verifyFamilyNameValidationErrorVisible() {
        familyNameLabel.click();
        familyNameError.shouldBe(Condition.visible, Duration.ofSeconds(8));
        return this;
    }

    public void verifyRegisterButtonIsVisible() {
        registerPatientButton.shouldBe(Condition.visible);
    }
}