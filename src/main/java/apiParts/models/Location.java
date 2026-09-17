package apiParts.models;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Location {
    OUTPATIENT_CLINIC(
            "44c3efb0-2583-4c80-a79e-1f756a03c0a1",
            "Outpatient Clinic"),
    INPATIENT_WARD(
            "ba685651-ed3b-4e63-9b35-78893060758a",
            "Inpatient Ward");

    @JsonValue
    private final String uuid;
    private final String display;
}
