package apiParts.models.allergy;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CodedAllergenUuid {
    ALLERGEN_UUID("162298AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");

    private final String allergen;
}