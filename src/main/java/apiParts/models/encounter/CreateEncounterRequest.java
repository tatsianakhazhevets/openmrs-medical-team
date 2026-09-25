package apiParts.models.encounter;

import apiParts.models.BaseModel;
import apiParts.models.EncounterType;
import apiParts.models.Location;
import apiParts.models.vitals.Obs;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreateEncounterRequest extends BaseModel {
    private String patient;  // patient uuid, created in test setup
    private EncounterType encounterType;
    private String visit;              // visit uuid, optional
    private String encounterDatetime;  // ISO-8601, must have if visit is present
    private Location location;
    private List<Obs> obs;
    private List<Object> orders;       // DrugOrder, TestOrder, etc.
}
