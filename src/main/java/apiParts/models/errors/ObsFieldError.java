package apiParts.models.errors;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

// ObsValidator errors: numeric value outside concept absolute range (lowAbsolute / hiAbsolute)
@Getter
@RequiredArgsConstructor
public enum ObsFieldError implements FieldError {
    VALUE_OUT_OF_RANGE_LOW("valueNumeric", "error.value.outOfRange.low"),
    VALUE_OUT_OF_RANGE_HIGH("valueNumeric", "error.value.outOfRange.high");

    private final String field;
    private final String code;
}
