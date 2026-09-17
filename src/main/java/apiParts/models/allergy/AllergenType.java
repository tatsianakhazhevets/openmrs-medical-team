package apiParts.models.allergy;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AllergenType {
    DRUG("DRUG");

    private final String drug;
}