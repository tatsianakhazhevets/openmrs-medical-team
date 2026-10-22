package apiTests;

import apiParts.assertions.SoftlyContext;
import com.codeborne.selenide.logevents.SelenideLogger;
import io.qameta.allure.selenide.AllureSelenide;
import common.extensions.*;
import common.storages.SessionStorage;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;

// Precondition extensions run before each test in this order (after each - in reverse).
// Each of them does nothing if its annotation (@CreatePatient, @CreateProcedure, @CreateOrder) is absent.
// Procedure and order need a patient, so CreatePatientExtension goes first.
@ExtendWith({
        CreatePatientExtension.class,
        CreateVisitExtension.class,
        CreateEncounterExtension.class,
        CreateProcedureExtension.class,
        CreateOrderExtension.class
})
public class BaseTest {
    private static final String ALLURE_SELENIDE = "AllureSelenide";
    protected SoftAssertions softly;

    // Allure:
    // - API: each request has a method/endpoint step; AllureRestAssured attaches its request / response
    // - UI: Selenide actions and checks become Allure steps, failed step gets screenshot + page source.
    //   Added before each test (not in @BeforeAll) - parallel tests run in different threads;
    //   re-adding a listener with the same name replaces it. API tests have no Selenide events - no effect.
    @BeforeEach
    public void addAllureSelenideListener() {
        SelenideLogger.addListener(ALLURE_SELENIDE, new AllureSelenide()
                .screenshots(true)
                .savePageSource(true));
    }

    @BeforeEach
    public void setUpTest(){
        this.softly = new SoftAssertions();
        SoftlyContext.set(this.softly);
    }

    @AfterEach
    public void afterTest() {
        SoftlyContext.clear();
        softly.assertAll();
    }
}