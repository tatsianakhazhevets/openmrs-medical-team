package apiParts.models;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum VitalsConcept implements HasUuid {
    SYSTOLIC_BP        ("5085AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", ValueType.NUMERIC), // mmHg
    DIASTOLIC_BP       ("5086AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", ValueType.NUMERIC), // mmHg
    RESPIRATORY_RATE   ("5242AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", ValueType.NUMERIC), // breaths/min
    OXYGEN_SATURATION  ("5092AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", ValueType.NUMERIC), // %
    PULSE              ("5087AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", ValueType.NUMERIC), // beats/min
    TEMPERATURE        ("5088AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", ValueType.NUMERIC), // °C
    GENERAL_NOTE       ("165095AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", ValueType.TEXT),
    WEIGHT             ("5089AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", ValueType.NUMERIC), // kg
    HEIGHT             ("5090AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", ValueType.NUMERIC), // cm
    MID_UPPER_ARM_CIRC ("1343AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", ValueType.NUMERIC), // cm
    BMI                ("1342AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", ValueType.NUMERIC); // kg/m²

    @JsonValue
    private final String uuid;
    private final ValueType valueType;

    public enum ValueType { NUMERIC, TEXT }
}