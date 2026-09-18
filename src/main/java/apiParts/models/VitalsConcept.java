package apiParts.models;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Concepts used in vitals & biometrics encounters.
 * <p>
 * lowAbsolute / hiAbsolute - absolute limits of the concept reference range (inclusive, ObsValidator).
 * Source: GET /obs?patient={uuid}&concept={uuid}&v=full -> referenceRange.
 * They are the single source of truth for boundary and out-of-range test data.
 * null means the concept has no absolute limits (TEXT concepts and MID_UPPER_ARM_CIRC).
 * decimalPlaces - precision of the concept, used when a random value inside the range is generated.
 */
@Getter
@RequiredArgsConstructor
public enum VitalsConcept {
    SYSTOLIC_BP        ("5085AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", ValueType.NUMERIC,   0.0, 250.0, 0), // mmHg
    DIASTOLIC_BP       ("5086AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", ValueType.NUMERIC,   0.0, 150.0, 0), // mmHg
    RESPIRATORY_RATE   ("5242AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", ValueType.NUMERIC,   0.0,  99.0, 0), // breaths/min
    OXYGEN_SATURATION  ("5092AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", ValueType.NUMERIC,   0.0, 100.0, 0), // %
    PULSE              ("5087AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", ValueType.NUMERIC,   0.0, 230.0, 0), // beats/min
    TEMPERATURE        ("5088AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", ValueType.NUMERIC,  25.0,  47.0, 0), // °C
    GENERAL_NOTE       ("165095AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", ValueType.TEXT,     null,  null, 0),
    WEIGHT             ("5089AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", ValueType.NUMERIC,   0.0, 250.0, 1), // kg
    HEIGHT             ("5090AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", ValueType.NUMERIC,  10.0, 272.0, 1), // cm
    // no absolute limits in reference range: any value is accepted, NOMINAL_* is used as a plausible range
    MID_UPPER_ARM_CIRC ("1343AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", ValueType.NUMERIC,  null,  null, 0), // cm
    BMI                ("1342AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", ValueType.NUMERIC,   0.0, 100.0, 1); // kg/m²

    // Fallback range for NUMERIC concepts without absolute limits: plausible values, not validated by server
    public static final double NOMINAL_LOW = 10.0;
    public static final double NOMINAL_HIGH = 50.0;

    @JsonValue
    private final String uuid;
    private final ValueType valueType;
    private final Double lowAbsolute;
    private final Double hiAbsolute;
    // decimal places the concept accepts: 0 - whole numbers only (allowDecimal = false)
    private final int decimalPlaces;

    public boolean hasAbsoluteRange() {
        return lowAbsolute != null && hiAbsolute != null;
    }

    // Lowest value the server accepts: absolute limit, or the nominal one when the concept has no limits
    public double low() {
        return lowAbsolute == null ? NOMINAL_LOW : lowAbsolute;
    }

    // Highest value the server accepts: absolute limit, or the nominal one when the concept has no limits
    public double high() {
        return hiAbsolute == null ? NOMINAL_HIGH : hiAbsolute;
    }

    // Value as it should be sent: whole number when the concept does not allow decimals
    public Number valueOf(double value) {
        return decimalPlaces == 0 ? (int) value : value;
    }

    public enum ValueType { NUMERIC, TEXT }
}
