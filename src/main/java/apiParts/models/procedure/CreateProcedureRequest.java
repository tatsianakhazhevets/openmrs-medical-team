package apiParts.models.procedure;

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
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreateProcedureRequest extends BaseModel {
    private String patient;                     // patient uuid, created in test setup
    private String procedureCoded;              // concept uuid, mutually exclusive with procedureNonCoded
    private String procedureNonCoded;           // free text instead of procedureCoded
    private String procedureType;
    private String bodySite;                    // concept uuid
    private String startDateTime;               // ISO-8601 with offset, e.g. 2026-09-17T22:00:00+03:00
    private String endDateTime;
    private String estimatedStartDate;          // yyyy / yyyy-MM / yyyy-MM-dd, mutually exclusive with startDateTime for new procedure
    private String status;                      // concept uuid
    private String notes;
    private Integer duration;
    private String durationUnit;                // concept uuid, required with duration
}
