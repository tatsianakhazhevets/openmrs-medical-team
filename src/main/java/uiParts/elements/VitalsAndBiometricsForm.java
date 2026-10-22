package uiParts.elements;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;
import org.openqa.selenium.Keys;
import uiParts.helpers.UiTimeouts;
import uiParts.models.VitalsFormData;
import uiParts.pages.VitalsAndBiometricsPage;


// "Record vitals and biometrics" workspace form. Inputs have generated ids like ":r2g:-temperature",
// so they are found by the stable id suffix.
public class VitalsAndBiometricsForm extends BaseElement {

    public VitalsAndBiometricsForm(SelenideElement selenideElement) {
        super(selenideElement);
    }

    public VitalsAndBiometricsForm shouldBeOpened() {
        selenideElement.shouldBe(Condition.visible, UiTimeouts.LONG);
        return this;
    }

    // Works both for an empty form and for the edit form prefilled with saved values
    public VitalsAndBiometricsForm fill(VitalsFormData data) {
        setInput("temperature", data.getTemperature());
        setInput("systolicBloodPressure", data.getSystolic());
        setInput("diastolicBloodPressure", data.getDiastolic());
        setInput("pulse", data.getPulse());
        setInput("respiratoryRate", data.getRespiratoryRate());
        setInput("oxygenSaturation", data.getOxygenSaturation());
        setInput("generalPatientNote", data.getNotes());
        setInput("weight", data.getWeight());
        setInput("height", data.getHeight());
        setInput("midUpperArmCircumference", data.getMidUpperArmCircumference());
        waitUntilSettled();
        return this;
    }

    // A click on "Save and close" right after typing can be lost: the last field still has focus and
    // the form is re-rendering. The button gives no "saving" state, so a lost click can not be told from
    // a slow save and the click must not be repeated (it would save the record twice).
    // Instead the form is let settle before the click: focus leaves the last field and the form has
    // recalculated BMI from the entered weight and height (its state is updated).
    private void waitUntilSettled() {
        input("midUpperArmCircumference").pressTab();
        input("computedBodyMassIndex").shouldNotHave(Condition.exactValue(""), UiTimeouts.LONG);
    }

    // read-only field, computed by the form from weight and height
    public String getBmi() {
        return input("computedBodyMassIndex").getValue();
    }

    // "Save and close": the workspace closes after a successful save, back to the tab with the tables
    public VitalsAndBiometricsPage save() {
        find("button[type='submit']").shouldBe(Condition.enabled).click();
        selenideElement.shouldNotBe(Condition.visible, UiTimeouts.LONG);
        return new VitalsAndBiometricsPage();
    }

    // exactValue check: a prefilled React input must be replaced, not appended to
    private void setInput(String idSuffix, Object value) {
        String text = String.valueOf(value);
        SelenideElement input = input(idSuffix);
        clear(input);
        input.sendKeys(text);
        input.shouldHave(Condition.exactValue(text));
    }

    // setValue / clear() do not reset a React controlled input: React restores the old value and the new one
    // is appended to it ("42" + "36.9" -> "4236.9"). Select all with the keyboard and delete instead.
    // Both shortcuts are sent: Ctrl+A for the browser on Linux / Windows (Selenoid), Cmd+A on macOS.
    private static void clear(SelenideElement input) {
        input.sendKeys(Keys.chord(Keys.CONTROL, "a"));
        input.sendKeys(Keys.chord(Keys.COMMAND, "a"));
        input.sendKeys(Keys.BACK_SPACE);
        input.shouldHave(Condition.exactValue(""));
    }

    private SelenideElement input(String idSuffix) {
        return find("[id$='-" + idSuffix + "']");
    }
}
