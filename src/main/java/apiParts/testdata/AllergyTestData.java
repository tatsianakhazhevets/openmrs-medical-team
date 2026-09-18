package apiParts.testdata;

import apiParts.generators.RandomModelGenerator;
import apiParts.models.allergy.Allergen;
import apiParts.models.allergy.AllergenType;
import apiParts.models.allergy.AllergyRequest;
import apiParts.models.allergy.CodedAllergen;
import apiParts.models.allergy.CodedAllergenUuid;
import apiParts.models.allergy.Reaction;
import apiParts.models.allergy.ReactionUuid;
import apiParts.models.allergy.ReactionWrapper;
import apiParts.models.allergy.Severity;
import apiParts.models.allergy.SeverityUuid;

import java.util.List;

/**
 * Requests of the standard allergy fixture (a Drug allergy with one reaction) used across
 * AllergyApiTests. Only the comment differs between calls, so callers can build the
 * "expected" response from the same request instance they send.
 */
public class AllergyTestData {

    private AllergyTestData() {
    }

    // Valid drug allergy (fixed allergen/severity/reaction) with a caller-supplied comment
    public static AllergyRequest allergyRequest(String comment) {
        return AllergyRequest.builder()
                .allergen(Allergen.builder()
                        .allergenType(AllergenType.DRUG.getDrug())
                        .codedAllergen(CodedAllergen.builder()
                                .uuid(CodedAllergenUuid.ALLERGEN_UUID.getAllergen())
                                .build())
                        .build())
                .severity(Severity.builder()
                        .uuid(SeverityUuid.SEVERITY_UUID.getSeverity())
                        .build())
                .comment(comment)
                .reactions(List.of(
                        ReactionWrapper.builder()
                                .reaction(Reaction.builder()
                                        .uuid(ReactionUuid.REACTION_UUID.getReaction())
                                        .build())
                                .build()))
                .build();
    }

    // Same standard allergy with a fresh random comment
    public static AllergyRequest allergyRequest() {
        return allergyRequest(RandomModelGenerator.randomSentence());
    }
}
