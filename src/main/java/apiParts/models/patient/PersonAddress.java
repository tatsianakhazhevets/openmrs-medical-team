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
public class PersonAddress extends BaseModel {
    @GeneratingRule(regex = "[A-Za-z0-9.,'/-](?:[A-Za-z0-9 .,'/-]{0,253}[A-Za-z0-9.,'/-])?")
    private String address1;

    @GeneratingRule(regex = "[A-Za-z0-9 .,'/-]{1,255}")
    private String cityVillage;

    @GeneratingRule(regex = "[A-Za-z0-9 .,'/-]{1,255}")
    private String country;

    @GeneratingRule(regex = "[A-Za-z0-9 -]{1,50}")
    private String postalCode;
}