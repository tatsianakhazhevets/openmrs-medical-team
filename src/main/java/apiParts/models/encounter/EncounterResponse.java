package apiParts.models.encounter;

import apiParts.models.BaseModel;
import apiParts.models.Link;
import apiParts.models.Ref;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response of POST / PUT / GET /ws/rest/v1/encounter, in both default and {@code ?v=full}
 * representation.
 * <p>
 * All nested resources (patient, location, encounterType, visit, form, obs, etc.) are
 * returned as {@link Ref} objects: short references with uuid and display name only,
 * not full resource objects. {@code ?v=full} adds more fields inside those references
 * (name, tags, ...) - they are ignored here, since only uuid and display are asserted on.
 * <p>
 * obs contain only "Concept name: value" in display, use GET /obs/{uuid} or ?v=full
 * to get concept and value.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class EncounterResponse extends BaseModel {
    private String uuid;
    private String display;
    private String encounterDatetime;

    // References to related resources (uuid + display)
    private Ref patient;
    private Ref location;
    private Ref form;                     // null for vitals encounter
    private Ref encounterType;
    private Ref visit;

    private List<Ref> obs;
    private List<Ref> orders;
    private List<Ref> encounterProviders;
    private List<Ref> diagnoses;

    private AuditInfo auditInfo;          // ?v=full only
    private List<Link> links;
    private Boolean voided;
    private String resourceVersion;
}
