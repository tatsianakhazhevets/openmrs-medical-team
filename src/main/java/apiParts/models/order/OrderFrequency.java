package apiParts.models.order;

import apiParts.models.HasUuid;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

// GET /orderfrequency - uuid of OrderFrequency object, not of its concept
@Getter
@RequiredArgsConstructor
public enum OrderFrequency implements HasUuid {
    ONCE_DAILY("136ebdb7-e989-47cf-8ec2-4e8b2ffe0ab3"),
    TWICE_DAILY("08c71152-c552-42e7-b094-f510ff44e9cb");

    @JsonValue
    private final String uuid;
}
