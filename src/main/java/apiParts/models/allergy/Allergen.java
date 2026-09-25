package apiParts.models.allergy;

import apiParts.generators.EnumGeneratingRule;
import apiParts.models.BaseModel;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class Allergen extends BaseModel {

    @EnumGeneratingRule(enumClass = AllergenType.class, valueMethod = "getValue")
    private String allergenType;

    private CodedAllergen codedAllergen;
}