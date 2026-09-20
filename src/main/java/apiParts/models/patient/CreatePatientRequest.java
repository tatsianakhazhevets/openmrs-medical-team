package apiParts.models.patient;

import apiParts.generators.CollectionGeneratingRule;
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
public class CreatePatientRequest extends BaseModel {
    private PersonRequest person;

    @CollectionGeneratingRule(minSize = 1, maxSize = 1)
    private List<PatientIdentifierRequest> identifiers;
}