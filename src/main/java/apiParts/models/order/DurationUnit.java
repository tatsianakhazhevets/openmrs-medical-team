package apiParts.models.order;

import apiParts.models.HasUuid;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

// Concepts for durationUnits (CIEL), GET /orderentryconfig -> durationUnits
@Getter
@RequiredArgsConstructor
public enum DurationUnit implements HasUuid {
    SECONDS("162583AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"),
    MINUTES("1733AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"),
    HOURS("1822AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"),
    DAYS("1072AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"),
    WEEKS("1073AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"),
    MONTHS("1074AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"),
    YEARS("1734AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"),
    OCCURRENCES("162582AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");   // duration = number of doses, period depends on frequency

    @JsonValue
    private final String uuid;
}
