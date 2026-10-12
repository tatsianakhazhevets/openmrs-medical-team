package common.extensions;

import apiParts.config.Config;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.WebDriverRunner;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.openqa.selenium.WebDriver;
import uiParts.helpers.SelenoidVideo;

/**
 * Browser lifecycle of UI tests. Mode - uiReuseBrowser (config.properties / -DuiReuseBrowser=true):
 * <p>
 * false (default) - new browser for every test, closed after it.
 * <p>
 * true - one browser per test thread, reused by the next test. OpenMRS frontend is heavy (dozens of JS modules):
 * a new browser downloads all of them, a reused one takes them from the cache. After a passed test the user state
 * is reset (tabs, SPA unloaded, localStorage / sessionStorage / cookies cleared), the cache stays.
 * The browser is still closed after a failed test (its state is unknown: hung renderer, open modal)
 * and when the reset itself fails.
 * <p>
 * Video (uiVideo=true, see SelenoidVideo): the file is complete only when the session is closed.
 * Failed test - the video is attached to Allure (with reuse it also shows previous tests of the session,
 * the failed one is at the end). Closed session of a passed test - the video is deleted.
 * With reuse, videos of sessions closed at JVM shutdown stay in the Selenoid video directory.
 */
public class BrowserSessionExtension implements AfterTestExecutionCallback, AfterEachCallback {
    private static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace.create(BrowserSessionExtension.class);
    private static final String TEST_FAILED = "testFailed";
    // light page of the app origin: unloads the SPA, keeps access to the origin's storage
    private static final String BLANK_PAGE_OF_APP_ORIGIN = "/favicon.ico";

    @Override
    public void afterTestExecution(ExtensionContext context) {
        context.getStore(NAMESPACE).put(TEST_FAILED, context.getExecutionException().isPresent());
    }

    // runs after @AfterEach methods of the test class
    @Override
    public void afterEach(ExtensionContext context) {
        if (!WebDriverRunner.hasWebDriverStarted()) {
            return;
        }
        boolean testFailed = context.getStore(NAMESPACE).getOrDefault(TEST_FAILED, Boolean.class, false);

        if (reuseBrowser() && !testFailed) {
            try {
                resetUserState();
                return;
            } catch (RuntimeException resetFailed) {
                // browser is not usable (e.g. hung renderer) - close it, the next test gets a new one
            }
        }
        closeBrowser(testFailed);
    }

    private static boolean reuseBrowser() {
        String reuse = Config.getProperty("uiReuseBrowser");
        return reuse != null && Boolean.parseBoolean(reuse.trim());
    }

    private static void closeBrowser(boolean testFailed) {
        // session id is read before closing; the video file is complete only after the session is closed
        String sessionId = SelenoidVideo.isEnabled() ? String.valueOf(Selenide.sessionId()) : null;

        Selenide.closeWebDriver();

        if (sessionId == null) {
            return;
        }
        if (testFailed) {
            SelenoidVideo.attachToAllure(sessionId);
        } else {
            SelenoidVideo.deleteInBackground(sessionId);
        }
    }

    private static void resetUserState() {
        WebDriver driver = WebDriverRunner.getWebDriver();
        String mainWindow = driver.getWindowHandle();
        for (String window : driver.getWindowHandles()) {
            if (!window.equals(mainWindow)) {
                driver.switchTo().window(window).close();
            }
        }
        driver.switchTo().window(mainWindow);

        Selenide.open(BLANK_PAGE_OF_APP_ORIGIN);
        Selenide.executeJavaScript("window.localStorage.clear(); window.sessionStorage.clear();");
        Selenide.clearBrowserCookies();
    }
}
