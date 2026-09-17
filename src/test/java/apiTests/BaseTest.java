package apiTests;

import common.extensions.CreateOrderExtension;
import common.extensions.CreatePatientExtension;
import common.extensions.CreateProcedureExtension;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;

// Precondition extensions run before each test in this order (after each - in reverse).
// Each of them does nothing if its annotation (@CreatePatient, @CreateProcedure, @CreateOrder) is absent.
// Procedure and order need a patient, so CreatePatientExtension goes first.
@ExtendWith({
        CreatePatientExtension.class,
        CreateProcedureExtension.class,
        CreateOrderExtension.class
})
public class BaseTest {
    protected SoftAssertions softly;

    @BeforeEach
    public void setUpTest(){
        this.softly = new SoftAssertions();
    }

    @AfterEach
    public void afterTest() {
        softly.assertAll();
    }
}