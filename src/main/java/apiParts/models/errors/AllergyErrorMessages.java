package apiParts.models.errors;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AllergyErrorMessages {

    INVALID_SUBMISSION("Invalid Submission"),
    ALLERGEN_REQUIRED("allergyapi.allergen.required"),
    SHOULD_USE_NEW_DELEGATE("allergen on class org.openmrs.Allergy => codedAllergen on class org.openmrs.Allergen => Should use newDelegate(SimpleObject) instead"),
    OBJECT_WITH_UUID_DOES_NOT_EXIST("Object with given uuid doesn't exist [null]");

    private final String message;
}