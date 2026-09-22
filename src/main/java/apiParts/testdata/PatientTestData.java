package apiParts.testdata;

import apiParts.generators.RandomModelGenerator;
import apiParts.models.patient.CreatePatientRequest;

/**
 * Request of the standard patient fixture (see apiParts.steps.AdminSteps#createPatient).
 */
public class PatientTestData {

    private PatientTestData() {
    }

    public static CreatePatientRequest createPatientRequest(String identifier) {
        CreatePatientRequest request = RandomModelGenerator.generate(CreatePatientRequest.class);
        return request;
    }

    public static CreatePatientRequest createPatientRequest() {
        CreatePatientRequest request = RandomModelGenerator.generate(CreatePatientRequest.class);
        return request;
    }
}