package apiParts.models.procedure;

import apiParts.models.BaseModel;
import apiParts.models.HasUuid;
import apiParts.models.encounter.Ref;
import apiParts.utils.DateTimeUtils;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Procedure as returned by POST /procedure and GET /procedure/{uuid} (default and full representations are the same).
 * procedureType is metadata: {uuid, name} instead of {uuid, display} - only uuid is read into Ref.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProcedureResponse extends BaseModel implements HasUuid {
    private String uuid;
    private String display;             // name of procedureCoded, e.g. "Laparoscopic cholecystectomy"

    private Ref patient;
    private Ref procedureType;
    private Ref encounter;
    private Ref procedureCoded;
    private String procedureNonCoded;
    private Ref bodySite;

    private String startDateTime;       // server returns UTC: 2026-09-17T19:00:00.000+0000
    private String estimatedStartDate;
    private String endDateTime;
    private Integer duration;
    private Ref durationUnit;

    private Ref status;
    private Ref outcomeCoded;
    private String outcomeNonCoded;
    private String notes;
    private String formNamespaceAndPath;
    private Boolean voided;

    // Server converts offset to UTC (+03:00 -> +0000): normalize to Instant on deserialization,
    // so the same moment sent and returned in different offsets is equal in comparison.
    // Request dates are normalized the same way by @instant converter (model-comparison.yml).
    public void setStartDateTime(String startDateTime) {
        this.startDateTime = DateTimeUtils.toInstantString(startDateTime);
    }

    public void setEndDateTime(String endDateTime) {
        this.endDateTime = DateTimeUtils.toInstantString(endDateTime);
    }
}
