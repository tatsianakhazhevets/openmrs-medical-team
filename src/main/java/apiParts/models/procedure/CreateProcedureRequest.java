package apiParts.models.procedure;

import apiParts.generators.GeneratingRule;
import apiParts.generators.GenerationStrategy;
import apiParts.models.BaseModel;
import apiParts.models.order.DurationUnit;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.experimental.FieldNameConstants;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Body of POST /ws/rest/v1/procedure (create) and POST /procedure/{uuid} (update), emrapi module.
 * <p>
 * References are plain uuid strings (not enums), so negative tests can send "" or non-existent uuid.
 * Valid values: ProcedureType, ProcedureConcept, BodySite, ProcedureStatus, order.DurationUnit -> getUuid().
 */
@Data
@FieldNameConstants   // Fields.<name> - field names for overrides, case names, generating rules
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreateProcedureRequest extends BaseModel {
    @GeneratingRule(strategy = GenerationStrategy.PATIENT_UUID)
    private String patient;                     // patient uuid, created in test setup

    @GeneratingRule(enumClass = ProcedureConcept.class)
    private String procedureCoded;              // concept uuid, mutually exclusive with procedureNonCoded

    @GeneratingRule(nullable = true)            // mutually exclusive with procedureCoded: set explicitly in tests
    private String procedureNonCoded;           // free text instead of procedureCoded

    @GeneratingRule(enumClass = ProcedureType.class)
    private String procedureType;

    @GeneratingRule(enumClass = BodySite.class)
    private String bodySite;                    // concept uuid

    @GeneratingRule(strategy = GenerationStrategy.DATE_RANGE_START)
    private String startDateTime;               // ISO-8601 with offset, e.g. 2026-09-17T22:00:00+03:00

    @GeneratingRule(strategy = GenerationStrategy.DATE_RANGE_END)
    private String endDateTime;                 // startDateTime + up to 10 hours

    @GeneratingRule(nullable = true)            // optional, mutually exclusive with startDateTime
    private String estimatedStartDate;          // yyyy / yyyy-MM / yyyy-MM-dd, mutually exclusive with startDateTime for new procedure

    @GeneratingRule(enumClass = ProcedureStatus.class)
    private String status;                      // concept uuid

    @GeneratingRule(regex = "[A-Za-z]{5,100}")
    private String notes;

    @GeneratingRule(strategy = GenerationStrategy.DURATION)
    private Integer duration;

    @GeneratingRule(enumClass = DurationUnit.class)
    private String durationUnit;                // concept uuid, required with duration
}
