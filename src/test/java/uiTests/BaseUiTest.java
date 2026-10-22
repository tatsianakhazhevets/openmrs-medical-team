package uiTests;

import apiParts.config.Config;
import apiTests.BaseTest;
import common.extensions.BrowserSessionExtension;
import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.junit5.TextReportExtension;
import uiParts.helpers.SelenoidVideo;
import uiParts.helpers.UiTimeouts;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Map;
// Browser lifecycle (new browser per test or reused browser - uiReuseBrowser) and video of failed tests: see BrowserSessionExtension
@ExtendWith({TextReportExtension.class, BrowserSessionExtension.class})
public class BaseUiTest extends BaseTest {

    @BeforeAll
    public static void setupBrowser() {
        Configuration.baseUrl = Config.getProperty("uiBaseUrl");
        Configuration.browser = Config.getProperty("uiBrowser");
        Configuration.browserSize = Config.getProperty("uiBrowserSize");
        Configuration.fastSetValue = false;
        Configuration.timeout = UiTimeouts.DEFAULT.toMillis();
        Configuration.headless = false;

        // uiRemote empty -> local browser; set -> run in Selenoid
        String remote = Config.getProperty("uiRemote");
        if (remote != null && !remote.isBlank()) {
            Configuration.remote = remote;
            Configuration.browserCapabilities.setCapability("selenoid:options",
                    Map.of("enableVNC", true, "enableLog", true, "enableVideo", SelenoidVideo.isEnabled())
            );
        }
    }
}
