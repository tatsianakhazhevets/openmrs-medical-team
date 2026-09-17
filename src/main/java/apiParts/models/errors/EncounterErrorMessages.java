package apiParts.models.errors;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EncounterErrorMessages {
    MISSING_PATIENT("Some required properties are missing: patient"),
    INVALID_SUBMISSION("Invalid Submission"),
    MISSING_ENCOUNTER_TYPE("Some required properties are missing: encounterType"),
    OBJECT_WITH_UUID_DOES_NOT_EXIST("Object with given uuid doesn't exist [null]");

    private final String message;
}