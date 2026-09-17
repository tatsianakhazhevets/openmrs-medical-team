package apiParts.models.encounter;

import apiParts.models.BaseModel;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response of POST /ws/rest/v1/encounter.
 * <p>
 * All nested resources (patient, location, encounterType, visit, form, obs, etc.)
 * are returned as {@link Ref} objects: short references with uuid and display name only,
 * not full resource objects.
 */
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateEncounterResponse extends BaseModel {
    private String uuid;
    private String display;
    private String encounterDatetime;

    // References to related resources (uuid + display)
    private Ref patient;
    private Ref location;
    private Ref form;                     // null for vitals encounter
    private Ref encounterType;
    private Ref visit;

    // Lists of references; obs contain only "Concept name: value" in display,
    // use GET /obs/{uuid} or ?v=full to get concept and value
    private List<Ref> obs;
    private List<Ref> orders;
    private List<Ref> encounterProviders;
    private List<Ref> diagnoses;

    private Boolean voided;
    private String resourceVersion;
}
