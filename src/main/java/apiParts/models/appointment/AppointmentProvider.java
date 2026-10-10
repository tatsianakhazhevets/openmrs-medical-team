package apiParts.models.appointment;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AppointmentProvider {
    SUPER_USER("37df6f03-020c-4ad8-bf5b-7c0d4f1aa713"),
    JAKE_DOCTOR("705f5791-07a7-44b8-932f-a81f3526fc98");

    @JsonValue
    private final String uuid;
}