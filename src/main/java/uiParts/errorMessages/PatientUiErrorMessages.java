package uiParts.errorMessages;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PatientUiErrorMessages {
    FAMILY_NAME_IS_REQUIRED("Family name is required");

    private final String message;
}