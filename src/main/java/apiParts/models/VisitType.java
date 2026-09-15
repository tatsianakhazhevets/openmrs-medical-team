package apiParts.models;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum VisitType {
    FACILITY_VISIT("7b0f5697-27e3-40c4-8bae-f4049abfb4ed");

    @JsonValue
    private final String uuid;
}
