package apiParts.models;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Location {
    OUTPATIENT_CLINIC("44c3efb0-2583-4c80-a79e-1f756a03c0a1");

    @JsonValue
    private final String uuid;
}
