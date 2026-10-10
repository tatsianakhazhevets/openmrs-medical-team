package uiTests;

import apiParts.config.Config;
import apiTests.BaseTest;
import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.junit5.TextReportExtension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Map;
@ExtendWith({TextReportExtension.class})
public class BaseUiTest extends BaseTest {

    @BeforeAll
    public static void setupBrowser() {
        Configuration.baseUrl = Config.getProperty("uiBaseUrl");
        Configuration.browser = Config.getProperty("uiBrowser");
        Configuration.browserSize = Config.getProperty("uiBrowserSize");
        Configuration.fastSetValue = false;
        Configuration.headless = false;

        // uiRemote empty -> local browser; set -> run in Selenoid
        String remote = Config.getProperty("uiRemote");
        if (remote != null && !remote.isBlank()) {
            Configuration.remote = remote;
            Configuration.browserCapabilities.setCapability("selenoid:options",
                    Map.of("enableVNC", true, "enableLog", true, "enableVideo", false)
            );
        }
    }

    @AfterEach
    public void tearDown() {
        Selenide.closeWebDriver();
    }
}
