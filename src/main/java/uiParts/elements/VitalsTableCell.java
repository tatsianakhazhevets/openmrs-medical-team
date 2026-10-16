package uiParts.elements;

import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;

// Cell of the vitals / biometrics table. Value is rendered in a div: abnormal values get
// a background on it and an arrow in its ::after pseudo element (not part of the text).
public class VitalsTableCell extends BaseElement {

    public VitalsTableCell(SelenideElement selenideElement) {
        super(selenideElement);
    }

    public String getText() {
        return selenideElement.getText().trim();
    }

    // computed background of the value, e.g. "rgba(255, 215, 217, 1)", transparent for normal values
    public String getBackgroundColor() {
        return value().getCssValue("background-color");
    }

    // arrow shown after the value: "↑", "↓↓" ..., empty for normal values
    public String getFlag() {
        String content = Selenide.executeJavaScript(
                "return getComputedStyle(arguments[0], '::after').content;", value());
        if (content == null || content.equals("none") || content.equals("normal")) {
            return "";
        }
        return content.replace("\"", "").trim();
    }

    private SelenideElement value() {
        return find("div");
    }
}
