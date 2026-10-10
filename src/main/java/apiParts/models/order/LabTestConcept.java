package apiParts.models.order;

import apiParts.models.HasUuid;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

// concept uuid used as testorder.concept, GET /concept/{uuid}
@Getter
@RequiredArgsConstructor
public enum LabTestConcept implements HasUuid {
    ALKALINE_PHOSPHATASE("785AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");

    @JsonValue
    private final String uuid;
}
