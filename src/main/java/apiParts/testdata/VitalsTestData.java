package apiParts.testdata;

import apiParts.generators.RandomModelGenerator;
import apiParts.models.VitalsConcept;
import apiParts.models.vitals.Obs;

import java.util.List;

import static apiParts.models.VitalsConcept.*;

/**
 * Obs of the standard vitals encounter (see apiParts.steps.AdminSteps#createVitalsEncounter).
 * Values are generated inside the reference range of each concept, see {@link VitalsConcept}.
 */
public class VitalsTestData {

    private VitalsTestData() {
    }

    // Every vitals concept with a random value inside its range.
    // Exposed so tests can assert the number of obs instead of hardcoding it
    public static List<Obs> vitalsObs() {
        return List.of(
                randomObs(SYSTOLIC_BP),
                randomObs(DIASTOLIC_BP),
                randomObs(RESPIRATORY_RATE),
                randomObs(OXYGEN_SATURATION),
                randomObs(PULSE),
                randomObs(TEMPERATURE),
                Obs.of(GENERAL_NOTE, RandomModelGenerator.randomSentence()),
                randomObs(WEIGHT),
                randomObs(HEIGHT),
                randomObs(MID_UPPER_ARM_CIRC),
                randomObs(BMI));
    }

    // Value inside the absolute range of the concept (nominal range when it has no limits)
    public static Obs randomObs(VitalsConcept concept) {
        return Obs.of(concept, concept.valueOf(RandomModelGenerator.randomDouble(
                concept.low(), concept.high(), concept.getDecimalPlaces())));
    }
}
