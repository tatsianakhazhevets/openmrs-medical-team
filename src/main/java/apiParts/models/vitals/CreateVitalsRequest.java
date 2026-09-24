package apiParts.models.vitals;

import apiParts.generators.EnumGeneratingRule;
import apiParts.generators.PatientUuidGeneratingRule;
import apiParts.generators.VitalsObsGeneratingRule;
import apiParts.models.*;
import apiParts.models.EncounterType;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreateVitalsRequest extends BaseModel {
    @PatientUuidGeneratingRule  // uuid of the patient from @CreatePatient
    private String patient;

    @EnumGeneratingRule(enumClass = VitalsUUID.class, valueMethod = "getValue")
    private VitalsUUID encounterType;

    @EnumGeneratingRule(enumClass = Location.class, valueMethod = "getValue")
    private Location location;

    @VitalsObsGeneratingRule  // one obs per VitalsConcept, random value inside its range
    private List<Obs> obs;
}
