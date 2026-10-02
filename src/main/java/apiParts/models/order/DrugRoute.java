package apiParts.models.order;

import apiParts.models.HasUuid;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

// Concepts for route (CIEL)
@Getter
@RequiredArgsConstructor
public enum DrugRoute implements HasUuid {
    ORAL("160240AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"),
    INTRAVENOUS("160242AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");

    @JsonValue
    private final String uuid;
}
