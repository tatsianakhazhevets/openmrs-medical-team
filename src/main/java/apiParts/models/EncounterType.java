package apiParts.models;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EncounterType {
    VITALS("67a71486-1a54-468f-ac3e-7091a9a79584"),
    ORDER("39da3525-afe4-45ff-8977-c53b7b359158");

    @JsonValue
    private final String uuid;
}
