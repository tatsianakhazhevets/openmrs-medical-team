package apiParts.models.encounter;

import apiParts.generators.GeneratingRule;
import apiParts.generators.GenerationStrategy;
import apiParts.models.BaseModel;
import apiParts.models.Location;
import apiParts.models.vitals.Obs;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.experimental.FieldNameConstants;
import lombok.*;

import java.util.List;

/**
 * POST / PUT /encounter body - the single encounter request model.
 * <p>
 * RandomModelGenerator gives an order encounter without orders: orders are set by the test
 * (e.g. {@code request.setOrders(List.of(order))}), vitals encounter has its own model CreateVitalsRequest.
 * encounterDatetime is not generated (server defaults it to now); tests that assert on it
 * set it explicitly, e.g. {@code request.setEncounterDatetime(RandomModelGenerator.pastDateTime())}.
 */
@Data
@FieldNameConstants   // Fields.<name> - field names for overrides, case names, generating rules
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreateEncounterRequest extends BaseModel {
    @GeneratingRule(strategy = GenerationStrategy.PATIENT_UUID)
    private String patient;  // patient uuid, created in test setup
    @GeneratingRule(enumClass = EncounterType.class, enumValue = "ORDER")
    private EncounterType encounterType;
    @GeneratingRule(nullable = true)
    private String visit;              // visit uuid, optional
    @GeneratingRule(nullable = true)
    private String encounterDatetime;  // ISO-8601, must have if visit is present
    @GeneratingRule(enumClass = Location.class)   // any location
    private Location location;
    @GeneratingRule(nullable = true)
    private List<Obs> obs;
    @GeneratingRule(nullable = true)
    private List<Object> orders;       // DrugOrder, TestOrder, etc.
}
