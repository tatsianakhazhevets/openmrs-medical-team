package apiParts.models.patient;

import apiParts.models.HasUuid;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum IdentifierType {
    TYPE("05a29f94-c0ed-11e2-94be-8c13b969e334");

    private final String type;
}