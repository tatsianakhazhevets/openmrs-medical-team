package apiParts.generators;

import apiParts.models.Location;
import apiParts.models.encounter.CreateEncounterRequest;
import apiParts.models.encounter.EncounterType;
import apiParts.models.order.LabTestConcept;
import apiParts.models.vitals.Obs;

import java.util.List;
import java.util.Map;

public final class LabResultGenerator {
    // Alkaline phosphatase is not configured as a "precise" concept, so the value must be a whole number
    private static final int MIN_ALKALINE_PHOSPHATASE = 1;
    private static final int MAX_ALKALINE_PHOSPHATASE = 270;

    private LabResultGenerator() {
    }

    // Order encounter update with a single Alkaline phosphatase result linked to the tested order
    public static CreateEncounterRequest generateAlkalinePhosphataseResult(String patientUUID, String orderUUID) {
        Double value = (double) RandomModelGenerator.randomInt(MIN_ALKALINE_PHOSPHATASE, MAX_ALKALINE_PHOSPHATASE);
        return RandomModelGenerator.generate(
                CreateEncounterRequest.class,
                Map.of(
                        CreateEncounterRequest.Fields.patient, patientUUID,
                        CreateEncounterRequest.Fields.encounterType, EncounterType.ORDER,
                        CreateEncounterRequest.Fields.location, Location.INPATIENT_WARD,
                        CreateEncounterRequest.Fields.obs, List.of(
                                Obs.ofLabResult(LabTestConcept.ALKALINE_PHOSPHATASE, orderUUID, value))
                ));
    }
}
