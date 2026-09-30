package apiParts.models.vitals;

import apiParts.generators.GeneratingRule;
import apiParts.generators.GenerationStrategy;
import apiParts.models.*;
import apiParts.models.encounter.EncounterType;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreateVitalsRequest extends BaseModel {
    @GeneratingRule(strategy = GenerationStrategy.PATIENT_UUID) // uuid of the patient from @CreatePatient
    private String patient;

    @GeneratingRule(enumClass = EncounterType.class, enumValue = "VITALS")
    private EncounterType encounterType;

    @GeneratingRule(nullable = true)
    private String visit;              // visit uuid, optional

    private Location location;

    @GeneratingRule(strategy = GenerationStrategy.VITALS_OBSERVATIONS)
    private List<Obs> obs;
}
