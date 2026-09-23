package apiParts.models.allergy;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CodedAllergenUuid {
    ACE_INHIBITORS("162298AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"),
    PENICILLINS("162297AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"),
    IBUPROFEN("77897AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"),
    PARACETAMOL("70116AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");

    private final String uuid;
}