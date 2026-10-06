package uiParts.pages;

import com.codeborne.selenide.SelenideElement;
import com.codeborne.selenide.WebDriverRunner;
import lombok.Getter;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$x;

@Getter
public class PatientRegistrationPage extends BasePage<PatientRegistrationPage> {
    private final SelenideElement givenNameInput = $("#givenName");
    private final SelenideElement familyNameInput = $("#familyName");
    private final SelenideElement genderMaleLabel = $x("//label[normalize-space()='Male']");
    private final SelenideElement genderFemaleLabel = $x("//label[normalize-space()='Female']");
    private final SelenideElement birthDateInput = $("#birthdate, input[placeholder='mm/dd/yyyy']");
    private final SelenideElement registerPatientButton = $x("//button[normalize-space()='Register patient']");
    private final SelenideElement nextStepButton = $x("//button[normalize-space()='Next']");

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
}