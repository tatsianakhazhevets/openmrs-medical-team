package apiParts.models.allergy;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SeverityUuid {
    SEVERE("1500AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"),
    MILD("1498AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"),
    MODERATE("1499AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");

    private final String uuid;
}