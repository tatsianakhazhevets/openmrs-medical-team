package apiParts.models.procedure;

import apiParts.generators.DurationGeneratingRule;
import apiParts.generators.EndDateGeneratingRule;
import apiParts.generators.EnumGeneratingRule;
import apiParts.generators.IgnoreGeneratingRule;
import apiParts.generators.PatientUuidGeneratingRule;
import apiParts.generators.StartDateGeneratingRule;
import apiParts.generators.StringGeneratingRule;
import apiParts.models.BaseModel;
import apiParts.models.order.DurationUnit;
import com.fasterxml.jackson.annotation.JsonInclude;
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
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreateProcedureRequest extends BaseModel {
    @PatientUuidGeneratingRule
    private String patient;                     // patient uuid, created in test setup

    @EnumGeneratingRule(enumClass = ProcedureConcept.class)
    private String procedureCoded;              // concept uuid, mutually exclusive with procedureNonCoded

    @IgnoreGeneratingRule                       // mutually exclusive with procedureCoded: set explicitly in tests
    private String procedureNonCoded;           // free text instead of procedureCoded

    @EnumGeneratingRule(enumClass = ProcedureType.class)
    private String procedureType;

    @EnumGeneratingRule(enumClass = BodySite.class)
    private String bodySite;                    // concept uuid

    @StartDateGeneratingRule
    private String startDateTime;               // ISO-8601 with offset, e.g. 2026-09-17T22:00:00+03:00

    @EndDateGeneratingRule
    private String endDateTime;                 // startDateTime + up to 10 hours

    @IgnoreGeneratingRule                       // optional, mutually exclusive with startDateTime
    private String estimatedStartDate;          // yyyy / yyyy-MM / yyyy-MM-dd, mutually exclusive with startDateTime for new procedure

    @EnumGeneratingRule(enumClass = ProcedureStatus.class)
    private String status;                      // concept uuid

    @StringGeneratingRule(regex = "[A-Za-z]{5,100}")
    private String notes;

    @DurationGeneratingRule
    private Integer duration;

    @EnumGeneratingRule(enumClass = DurationUnit.class)
    private String durationUnit;                // concept uuid, required with duration
}
