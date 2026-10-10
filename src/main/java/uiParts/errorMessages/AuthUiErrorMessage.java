package uiParts.errorMessages;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AuthUiErrorMessage {
    INVALID_USERNAME_OR_PASSWORD("Invalid username or password");

    private final String message;
}