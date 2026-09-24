package apiParts.models.euncouterTest;

import apiParts.generators.DateGeneratingRule;
import apiParts.generators.EnumGeneratingRule;
import apiParts.generators.PatientUuidGeneratingRule;
import apiParts.generators.StringGeneratingRule;
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
public class EncounterTestRequest extends BaseModel {
    @PatientUuidGeneratingRule
    private String patient;

    @EnumGeneratingRule(enumClass = EncounterTypeForApi.class, valueMethod = "getUuid")
    private String encounterType;

    @DateGeneratingRule(minYear = 1926, maxYear = 2025)
    private String encounterDatetime;

    @EnumGeneratingRule(enumClass = Location.class, valueMethod = "getUuid")
    private String location;
}