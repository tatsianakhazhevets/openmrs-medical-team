package apiParts.models.allergy;

import apiParts.generators.CollectionGeneratingRule;
import apiParts.generators.StringGeneratingRule;
import apiParts.models.BaseModel;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class AllergyRequest extends BaseModel {
    private Allergen allergen;

    private Severity severity;

    @StringGeneratingRule(regex = "[A-Za-z]{5,100}")
    private String comment;

    @CollectionGeneratingRule(minSize = 1, maxSize = 1)
    private List<ReactionWrapper> reactions;
}