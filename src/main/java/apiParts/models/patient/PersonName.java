package apiParts.models.patient;

import apiParts.generators.StringGeneratingRule;
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
public class PersonName {
    @StringGeneratingRule(regex = "[A-Za-z]{2,50}")
    private String givenName;

    @StringGeneratingRule(regex = "[A-Za-z]{2,50}")
    private String familyName;
}