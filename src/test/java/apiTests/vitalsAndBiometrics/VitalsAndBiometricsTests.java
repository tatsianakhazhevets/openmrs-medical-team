package apiTests.vitalsAndBiometrics;

import apiParts.models.EncounterType;
import apiParts.models.Location;
import apiParts.models.patient.CreateEncounterRequest;
import apiParts.models.patient.CreateEncounterRequest.Obs;
import apiParts.skelethon.endpoints.Endpoint;
import apiParts.skelethon.requests.common.SuccessfulCrudRequester;
import apiParts.specs.RequestSpecs;
import apiParts.specs.ResponseSpecs;
import apiParts.steps.AdminSteps;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static apiParts.models.VitalsConcept.*;

public class VitalsAndBiometricsTests {
    private String patientUUID;

    @BeforeEach
    void setUp() {
        var response = AdminSteps.createPatient();
        patientUUID = response.getUuid();
    }

    @Test
    public void adminCanAddVitalsAndBiometrics(){

        var request = CreateEncounterRequest.builder()
                .patient(patientUUID)
                .encounterType(EncounterType.VITALS)
                .location(Location.OUTPATIENT_CLINIC)
                .obs(List.of(
                        Obs.of(SYSTOLIC_BP, 100),
                        Obs.of(DIASTOLIC_BP, 70),
                        Obs.of(RESPIRATORY_RATE, 14),
                        Obs.of(OXYGEN_SATURATION, 95),
                        Obs.of(PULSE, 68),
                        Obs.of(TEMPERATURE, 37),
                        Obs.of(GENERAL_NOTE, "Some note"),
                        Obs.of(WEIGHT, 90.2),
                        Obs.of(HEIGHT, 177.3),
                        Obs.of(MID_UPPER_ARM_CIRC, 14),
                        Obs.of(BMI, 28.7)
                ))
                .build();

        new SuccessfulCrudRequester<CreateEncounterRequest>(
                RequestSpecs.adminSpec(),
                Endpoint.ENCOUNTER_POST,
                ResponseSpecs.requestReturnsCreated()
        )
                .create(request);
    }

    //TODO - change values to random in positive boundaries

}
