package apiParts.models.appointment;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AppointmentKind {
    SCHEDULED("Scheduled");

    private final String value;
}