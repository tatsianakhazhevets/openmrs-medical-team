package uiParts.models;

import apiParts.generators.RandomModelGenerator;
import apiParts.models.vitals.VitalsConcept;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import static uiParts.pages.VitalsAndBiometricsPage.*;

/**
 * Critical / normal limits of the vitals that the UI highlights - for an ADULT patient with gender M or F.
 * Source: GET /concept/{uuid}?v=custom:(referenceRanges:(criteria,...)). The server has several ranges per concept
 * with criteria on patient age (and gender for BMI): children have other limits, BMI has no range for gender UNKNOWN.
 * Tests that check highlighting create the patient with @CreatePatient(gender = "M", adult = true).
 * Normal limits are inclusive (120 / 80 is shown as normal). null - the concept has no such limit
 * (SpO2 has no upper limits). Weight, height and MUAC have no limits and are never highlighted.
 * <p>
 * Values for a case are generated one step away from the limits, so they never hit a boundary.
 * Critical values go not further than one normal-to-critical distance, so they stay plausible
 * (and inside the form min / max).
 */
@Getter
@RequiredArgsConstructor
public enum VitalsReferenceRange {
    TEMPERATURE(VitalsConcept.TEMPERATURE, TEMPERATURE_COLUMN, false, 35.0, 36.0, 37.7, 38.0, 1),
    SYSTOLIC(VitalsConcept.SYSTOLIC_BP, BLOOD_PRESSURE_COLUMN, false, 90.0, 100.0, 120.0, 180.0, 0),
    DIASTOLIC(VitalsConcept.DIASTOLIC_BP, BLOOD_PRESSURE_COLUMN, false, 50.0, 60.0, 80.0, 110.0, 0),
    PULSE(VitalsConcept.PULSE, PULSE_COLUMN, false, 45.0, 60.0, 100.0, 110.0, 0),
    RESPIRATORY_RATE(VitalsConcept.RESPIRATORY_RATE, RESPIRATORY_RATE_COLUMN, false, 10.0, 12.0, 18.0, 24.0, 0),
    OXYGEN_SATURATION(VitalsConcept.OXYGEN_SATURATION, OXYGEN_SATURATION_COLUMN, false, 90.0, 95.0, null, null, 0),
    BMI(VitalsConcept.BMI, BMI_COLUMN, true, 16.0, 18.5, 24.9, 30.0, 1);

    private final VitalsConcept concept;
    private final String column;
    private final boolean biometrics;   // column is in the biometrics table
    private final Double lowCritical;
    private final Double lowNormal;
    private final Double hiNormal;
    private final Double hiCritical;
    private final int scale;

    // SpO2 cannot be high: it has no upper limits
    public boolean supports(VitalsInterpretation interpretation) {
        return switch (interpretation) {
            case HIGH, CRITICALLY_HIGH -> hiNormal != null && hiCritical != null;
            case LOW, CRITICALLY_LOW -> lowNormal != null && lowCritical != null;
            case NORMAL -> true;
        };
    }

    public double randomValue(VitalsInterpretation interpretation) {
        double[] bounds = bounds(interpretation);
        return RandomModelGenerator.randomDouble(bounds[0], bounds[1], scale);
    }

    public boolean isIn(VitalsInterpretation interpretation, double value) {
        double[] bounds = bounds(interpretation);
        return value >= bounds[0] && value <= bounds[1];
    }

    // [min, max] of the values that get this interpretation, one step away from the limits
    private double[] bounds(VitalsInterpretation interpretation) {
        if (!supports(interpretation)) {
            throw new IllegalArgumentException(this + " has no " + interpretation + " values");
        }
        double step = scale == 0 ? 1 : 0.1;
        return switch (interpretation) {
            case NORMAL -> new double[]{lowNormal, hiNormal == null ? 100 : hiNormal};
            case HIGH -> new double[]{hiNormal + step, hiCritical - step};
            case CRITICALLY_HIGH -> new double[]{hiCritical + step, hiCritical + (hiCritical - hiNormal)};
            case LOW -> new double[]{lowCritical + step, lowNormal - step};
            case CRITICALLY_LOW -> new double[]{lowCritical - (lowNormal - lowCritical), lowCritical - step};
        };
    }
}
