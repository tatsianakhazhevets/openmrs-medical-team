package apiParts.models.order;

import apiParts.models.HasUuid;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

// Concepts for doseUnits / quantityUnits (CIEL), GET /orderentryconfig -> drugDosingUnits / drugDispensingUnits
@Getter
@RequiredArgsConstructor
public enum DosingUnit implements HasUuid {
    TABLET("1513AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"),   // dosing + dispensing
    BOTTLE("162353AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");   // dispensing only: valid for quantityUnits, invalid for doseUnits

    @JsonValue
    private final String uuid;
}
