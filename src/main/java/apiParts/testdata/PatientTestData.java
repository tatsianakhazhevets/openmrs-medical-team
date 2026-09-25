package apiParts.testdata;

import apiParts.generators.RandomModelGenerator;
import apiParts.models.patient.CreatePatientRequest;

import java.util.List;

/**
 * Request of the standard patient fixture (see apiParts.steps.AdminSteps#createPatient).
 */
public class PatientTestData {

    private PatientTestData() {
    }

    public static CreatePatientRequest createPatientRequest(String identifier) {
        CreatePatientRequest request = RandomModelGenerator.generate(CreatePatientRequest.class);
        request.getPerson().setAddresses(List.of());
        return request;
    }

    public static CreatePatientRequest createPatientRequest() {
        CreatePatientRequest request = RandomModelGenerator.generate(CreatePatientRequest.class);
        request.getPerson().setAddresses(List.of());
        return request;
    }
}