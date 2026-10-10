package apiParts.models.patient;

import apiParts.models.HasUuid;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

// Location of a patient identifier, GET /location?tag=Login%20Location.
// Not the same object as apiParts.models.Location (encounter / visit location)
@Getter
@RequiredArgsConstructor
public enum IdentifierLocation implements HasUuid {
    OUTPATIENT_CLINIC("dbdaabf6-a326-4804-aba7-062073e05cd1");

    @JsonValue
    private final String uuid;
}
