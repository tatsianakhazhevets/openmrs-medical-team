package apiParts.models.procedure;

import apiParts.generators.EnumGeneratingRule;
import apiParts.generators.GeneratedBy;
import apiParts.generators.SkipGeneration;
import apiParts.generators.suppliers.CurrentPatientUuid;
import apiParts.generators.suppliers.ProcedureStartDateTime;
import apiParts.models.BaseModel;
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
 * RandomModelGenerator: valid coded procedure started in the past, optional dates and duration are not generated.
 */
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreateProcedureRequest extends BaseModel {
    @GeneratedBy(CurrentPatientUuid.class)
    private String patient;                     // patient uuid, created in test setup

    @EnumGeneratingRule(enumClass = ProcedureConcept.class)
    private String procedureCoded;              // concept uuid, mutually exclusive with procedureNonCoded

    @SkipGeneration
    private String procedureNonCoded;           // free text instead of procedureCoded

    @EnumGeneratingRule(enumClass = ProcedureType.class)
    private String procedureType;

    @EnumGeneratingRule(enumClass = BodySite.class)
    private String bodySite;                    // concept uuid

    @GeneratedBy(ProcedureStartDateTime.class)
    private String startDateTime;               // ISO-8601 with offset, e.g. 2026-09-17T22:00:00+03:00

    @SkipGeneration
    private String endDateTime;

    @SkipGeneration
    private String estimatedStartDate;          // yyyy / yyyy-MM / yyyy-MM-dd, mutually exclusive with startDateTime for new procedure

    @EnumGeneratingRule(enumClass = ProcedureStatus.class)
    private String status;                      // concept uuid

    private String notes;                       // random word

    @SkipGeneration
    private Integer duration;

    @SkipGeneration
    private String durationUnit;                // concept uuid, required with duration
}
