package apiParts.models.patient;

import apiParts.generators.BooleanGeneration;
import apiParts.generators.GeneratingRule;
import apiParts.generators.GenerationStrategy;
import apiParts.models.BaseModel;
import apiParts.models.Location;
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
public class PatientIdentifierRequest extends BaseModel {
    @GeneratingRule(strategy = GenerationStrategy.IDENTIFIER, sourceField = "identifierType")
    private String identifier;

    @GeneratingRule(enumClass = IdentifierType.class)
    private String identifierType;

    @GeneratingRule(enumClass = Location.class)
    private String location;

    @GeneratingRule(booleanValue = BooleanGeneration.TRUE)
    private Boolean preferred;
}