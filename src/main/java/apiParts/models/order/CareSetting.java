package apiParts.models.order;

import apiParts.models.HasUuid;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

// GET /caresetting
@Getter
@RequiredArgsConstructor
public enum CareSetting implements HasUuid {
    OUTPATIENT("6f0c9a92-6f24-11e3-af88-005056821db0"),
    INPATIENT("c365e560-c3ec-11e3-9c1a-0800200c9a66");

    @JsonValue
    private final String uuid;
}
