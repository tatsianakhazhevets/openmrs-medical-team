package apiParts.models;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum VisitLocation {
    UBUNTU_HOSPITAL("f47ac10b-58cc-4372-a567-0e02b2c3d479");
    @JsonValue
    private final String uuid;
}
