package apiParts.models.allergy;

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
public class AllergyResponse extends BaseModel {
    private String uuid;
    private Allergen allergen;
    private Severity severity;
    private String comment;
    private List<ReactionWrapper> reactions;
}