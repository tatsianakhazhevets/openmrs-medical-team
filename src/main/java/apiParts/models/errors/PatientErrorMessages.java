package apiParts.models.errors;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PatientErrorMessages {

    PERSON_IS_MISSING("The person property is missing"),
    IDENTIFIER_CANNOT_INVOKE("identifiers on class org.openmrs.Patient => Cannot invoke \"java.util.Collection.iterator()\" because \"c\" is null"),
    OBJECT_WITH_UUID_DOES_NOT_EXIST("Object with given uuid doesn't exist [null]");

    private final String message;
}