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
public class PersonAddress {
    @StringGeneratingRule(regex = "[A-Za-z0-9.,'/-](?:[A-Za-z0-9 .,'/-]{0,253}[A-Za-z0-9.,'/-])?")
    private String address1;

    @StringGeneratingRule(regex = "[A-Za-z0-9 .,'/-]{1,255}")
    private String cityVillage;

    @StringGeneratingRule(regex = "[A-Za-z0-9 .,'/-]{1,255}")
    private String country;

    @StringGeneratingRule(regex = "[A-Za-z0-9 -]{1,50}")
    private String postalCode;
}