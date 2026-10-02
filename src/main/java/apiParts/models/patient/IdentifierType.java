package apiParts.models.patient;

import apiParts.models.HasUuid;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

// GET /patientidentifiertype?v=custom:(uuid,name,required,uniquenessBehavior,locationBehavior)
@Getter
@RequiredArgsConstructor
public enum IdentifierType implements HasUuid {
    MRS_ID("05a29f94-c0ed-11e2-94be-8c13b969e334");

    @JsonValue
    private final String uuid;
}