package apiParts.models.euncouterTest;

import apiParts.generators.*;
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
    @GeneratingRule(strategy = GenerationStrategy.PATIENT_UUID)
    private String patient;

    @GeneratingRule(enumClass = EncounterTypeForApi.class)
    private String encounterType;

    @GeneratingRule(strategy = GenerationStrategy.DATE, minYear = 1926, maxYear = 2025)
    private String encounterDatetime;

    @GeneratingRule(enumClass = Location.class)
    private String location;
}