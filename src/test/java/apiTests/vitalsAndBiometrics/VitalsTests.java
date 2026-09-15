package apiTests.vitalsAndBiometrics;

import apiParts.steps.AdminSteps;
import org.junit.jupiter.api.BeforeEach;

public class VitalsTests {

    @BeforeEach
    void setUp() {
        AdminSteps.createPatient();
        //start visit method
    }

    public void adminCanAddVitalsAndBiometrics(){

    }

}
