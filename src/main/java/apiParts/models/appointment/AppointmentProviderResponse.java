package apiParts.models.appointment;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AppointmentProviderResponse {
    ACCEPTED("ACCEPTED"),
    CANCELLED("CANCELLED");

    private final String value;
}
