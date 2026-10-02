package apiParts.models.errors;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EncounterErrorMessages {
    MISSING_PATIENT("Some required properties are missing: patient"),
    INVALID_SUBMISSION("Invalid Submission"),
    PATIENT_IS_REQUIRES("Patient is Required"),
    MISSING_ENCOUNTER_TYPE("Some required properties are missing: encounterType"),
    ENCOUNTER_TYPE_IS_REQUIRED("Encounter type is Required"),
    ENCOUNTER_DATETIME_SHOULD_BE_BEFORE_THE_CURRENT_DATA("The encounter datetime should be before the current date."),
    OBJECT_WITH_UUID_DOES_NOT_EXIST("Object with given uuid doesn't exist [null]");

    private final String message;
}