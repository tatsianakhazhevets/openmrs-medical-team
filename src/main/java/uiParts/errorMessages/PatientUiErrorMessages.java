package uiParts.errorMessages;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PatientUiErrorMessages {
    FAMILY_NAME_IS_REQUIRED("Family name is required"),
    GIVEN_NAME_IS_REQUIRED("Given name is required"),
    PLEASE_FILL_OUT_THIS_FIELD("Please fill out this field"),
    BIRTHDAY_IS_REQUIRED("Birthday is required");

    private final String message;
}