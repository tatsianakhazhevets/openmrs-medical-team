package uiParts.elements;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;
import uiParts.helpers.UiTimeouts;

import java.time.Duration;

// Single left sidebar item of the patient chart: div[data-extension-id] inside patient-chart-dashboard-slot
public class PatientChartDashboardSlot extends BaseElement {
    private static final String ACTIVE_CLASS = "active-left-nav-link";
    // the wrapper div appears first, the link inside is mounted by the app later (slow under parallel load)
    private static final Duration MOUNT_TIMEOUT = UiTimeouts.LONG;

    public PatientChartDashboardSlot(SelenideElement selenideElement) {
        super(selenideElement);
    }

    public String getExtensionId() {
        return selenideElement.getAttribute("data-extension-id");
    }

    public String getName() {
        return link().getText().trim();
    }

    public void open() {
        link().shouldBe(Condition.visible, MOUNT_TIMEOUT).click();
    }

    public boolean isActive() {
        return link().has(Condition.cssClass(ACTIVE_CLASS));
    }

    public void shouldBeActive() {
        link().shouldHave(Condition.cssClass(ACTIVE_CLASS), MOUNT_TIMEOUT);
    }

    private SelenideElement link() {
        return find("a");
    }
}
