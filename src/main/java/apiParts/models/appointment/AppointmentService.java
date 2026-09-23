package apiParts.models.appointment;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AppointmentService {
    GENERAL_MEDICINE("7ba3aa21-cc56-47ca-bb4d-a60549f666c0");

    @JsonValue
    private final String uuid;
}