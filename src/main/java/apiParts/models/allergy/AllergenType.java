package apiParts.models.allergy;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AllergenType {
    DRUG("DRUG"),
    FOOD("FOOD"),
    ENVIRONMENT("ENVIRONMENT"),
    OTHER("OTHER");

    private final String value;
}