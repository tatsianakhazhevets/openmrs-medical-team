package uiParts.helpers;

import apiParts.config.Config;
import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import com.codeborne.selenide.ex.UIAssertionError;
import io.qameta.allure.Allure;
import org.openqa.selenium.TimeoutException;

/**
 * Opens a page and retries the load when the app did not render.
 * <p>
 * OpenMRS loads dozens of JS modules on the first page load; sometimes the load hangs:
 * Chrome stops responding ("Timed out receiving message from renderer") or the page stays blank.
 * Reloading is safe - it does not change any data - so the load is retried (uiPageLoadAttempts, default 2).
 * <p>
 * Only page loads are retried, never actions (clicks, saves): a repeated action may change data twice.
 * Every retry is a separate Allure step, so retries are visible in the report.
 */
public final class PageLoader {
    private static final int ATTEMPTS = Integer.parseInt(
            Config.getProperty("uiPageLoadAttempts") == null ? "2" : Config.getProperty("uiPageLoadAttempts").trim());

    private PageLoader() {
    }

    // readyMarker - element that appears only when the app has rendered the page
    public static void open(String url, SelenideElement readyMarker) {
        for (int attempt = 1; ; attempt++) {
            try {
                Selenide.open(url);
                readyMarker.shouldBe(Condition.visible, UiTimeouts.LONG);
                return;
            } catch (TimeoutException | UIAssertionError notLoaded) {
                if (attempt >= ATTEMPTS) {
                    throw notLoaded;
                }
                String reason = notLoaded.getMessage() == null ? notLoaded.getClass().getSimpleName()
                        : notLoaded.getMessage().lines().findFirst().orElse("");
                Allure.step("Page did not load (attempt " + attempt + " of " + ATTEMPTS + "), reloading: " + reason);
            }
        }
    }
}
