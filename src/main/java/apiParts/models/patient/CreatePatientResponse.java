package apiParts.models.patient;

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
public class CreatePatientResponse extends BaseModel {
    private String uuid;
    private String display;
    private List<PatientIdentifierResponse> identifiers;
    private PersonResponse person;
}