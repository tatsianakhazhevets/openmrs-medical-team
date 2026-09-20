package apiParts.models.patient;

import apiParts.generators.BooleanGeneratingRule;
import apiParts.generators.CollectionGeneratingRule;
import apiParts.generators.DateGeneratingRule;
import apiParts.generators.StringGeneratingRule;
import apiParts.models.BaseModel;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PersonRequest extends BaseModel {
    @StringGeneratingRule(regex = "(M|F|UNKNOWN)")
    private String gender;

    @DateGeneratingRule(minYear = 1926, maxYear = 2025)
    private String birthdate;

    @BooleanGeneratingRule(false)
    private Boolean birthdateEstimated;

    @BooleanGeneratingRule(false)
    private Boolean dead;

    @CollectionGeneratingRule(minSize = 1, maxSize = 1)
    private List<PersonName> names;

    @CollectionGeneratingRule(minSize = 1, maxSize = 1)
    private List<PersonAddress> addresses;
}