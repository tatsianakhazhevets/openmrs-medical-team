package apiParts.models.patient;

import apiParts.generators.BooleanGeneration;
import apiParts.generators.GeneratingRule;
import apiParts.generators.GenerationStrategy;
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
    @GeneratingRule(regex = "(M|F|UNKNOWN)")
    private String gender;

    @GeneratingRule(strategy = GenerationStrategy.DATE, minYear = 1926, maxYear = 2025)
    private String birthdate;

    @GeneratingRule(booleanValue = BooleanGeneration.FALSE)
    private Boolean birthdateEstimated;

    @GeneratingRule(booleanValue = BooleanGeneration.FALSE)
    private Boolean dead;

    @GeneratingRule(minSize = 1, maxSize = 1)
    private List<PersonName> names;

    @GeneratingRule(minSize = 1, maxSize = 1)
    private List<PersonAddress> addresses;
}