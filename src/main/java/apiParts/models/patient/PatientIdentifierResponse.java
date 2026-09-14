package apiParts.models.patient;

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
public class PatientIdentifierResponse {
    private String uuid;
    private String identifier;
    private String identifierType;
    private String location;
    private Boolean preferred;
}