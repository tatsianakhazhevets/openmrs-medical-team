package apiParts.models.patient;

import apiParts.generators.GeneratingRule;
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
public class PersonName extends BaseModel {
    @GeneratingRule(regex = "[A-Za-z]{2,50}")
    private String givenName;

    @GeneratingRule(regex = "[A-Za-z]{2,50}")
    private String familyName;
}