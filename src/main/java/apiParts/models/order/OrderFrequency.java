package apiParts.models.order;

import apiParts.models.HasUuid;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

// GET /orderfrequency - uuid of OrderFrequency object, not of its concept.
// dosesPerDay is part of the frequency definition: autoExpireDate of an order with
// durationUnits = OCCURRENCES is calculated as duration (number of doses) / dosesPerDay
@Getter
@RequiredArgsConstructor
public enum OrderFrequency implements HasUuid {
    ONCE_DAILY("136ebdb7-e989-47cf-8ec2-4e8b2ffe0ab3", 1),
    TWICE_DAILY("08c71152-c552-42e7-b094-f510ff44e9cb", 2);

    @JsonValue
    private final String uuid;
    private final int dosesPerDay;
}
