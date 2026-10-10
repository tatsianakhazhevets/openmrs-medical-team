package apiParts.models.appointment;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AppointmentStatus {
    SCHEDULED("Scheduled"),
    CHECKED_IN("CheckedIn"),
    CANCELLED("Cancelled");

    private final String value;
}