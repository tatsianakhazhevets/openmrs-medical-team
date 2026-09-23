package apiParts.models.allergy;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReactionUuid {
    UNKNOWN("1067AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"),
    HEADACHE("139084AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");

    private final String Reaction;
}