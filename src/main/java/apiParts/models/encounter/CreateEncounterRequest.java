package apiParts.models.encounter;

import apiParts.generators.EnumGeneratingRule;
import apiParts.generators.FixedEnumGeneratingRule;
import apiParts.generators.IgnoreGeneratingRule;
import apiParts.generators.PatientUuidGeneratingRule;
import apiParts.models.BaseModel;
import apiParts.models.EncounterType;
import apiParts.models.Location;
import apiParts.models.vitals.Obs;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.List;

/**
 * RandomModelGenerator gives an order encounter without orders: orders are set by the test
 * (e.g. {@code request.setOrders(List.of(order))}), vitals encounter has its own model CreateVitalsRequest.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreateEncounterRequest extends BaseModel {
    @PatientUuidGeneratingRule
    private String patient;  // patient uuid, created in test setup
    @FixedEnumGeneratingRule(enumClass = EncounterType.class, value = "ORDER")
    private EncounterType encounterType;
    @IgnoreGeneratingRule
    private String visit;              // visit uuid, optional
    @IgnoreGeneratingRule
    private String encounterDatetime;  // ISO-8601, must have if visit is present
    @EnumGeneratingRule(enumClass = Location.class)
    private Location location;
    @IgnoreGeneratingRule
    private List<Obs> obs;
    @IgnoreGeneratingRule
    private List<Object> orders;       // DrugOrder, TestOrder, etc.
}
