package apiParts.models.vitals;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum VitalsUUID {
    VITALS("67a71486-1a54-468f-ac3e-7091a9a79584");

    @JsonValue
    private final String uuid;
}
