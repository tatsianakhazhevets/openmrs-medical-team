package apiParts.models.visit;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum VisitLocation {
    UBUNTU_HOSPITAL("f47ac10b-58cc-4372-a567-0e02b2c3d479"),
    MOBILE_CLINIC("8d9045ad-50f0-45b8-93c8-3ed4bce19dbf");
    @JsonValue
    private final String uuid;
}
