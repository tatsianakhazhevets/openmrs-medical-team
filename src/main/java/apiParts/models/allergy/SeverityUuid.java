package apiParts.models.allergy;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SeverityUuid {
    SEVERITY_UUID("1500AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");

    private final String severity;
}