package apiParts.models.visit;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum VisitAttributeType {
    INSURANCE_POLICY_NUMBER(
            "aac48226-d143-4274-80e0-264db4e368ee",
            "Insurance Policy Number");

    @JsonValue
    private final String uuid;
    private final String display;
}
