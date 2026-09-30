package apiParts.models.visit;

import apiParts.generators.GeneratingRule;
import apiParts.models.BaseModel;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UpdateVisitRequest extends BaseModel {

    private VisitType visitType;

    @GeneratingRule(regex = "[A-Za-z ]{5,100}")
    private String indication;
}
