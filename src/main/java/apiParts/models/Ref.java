package apiParts.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Lightweight reference to a related OpenMRS resource.
 * <p>
 * In the default representation OpenMRS does not embed full nested objects
 * (patient, location, visit, obs, encounterType, concept, drug, etc.). It returns a short
 * reference that contains only the resource uuid, a human-readable display name and links.
 * Use the uuid to fetch the full resource via its own endpoint if needed.
 * <p>
 * Example:
 * <pre>
 * { "uuid": "44c3efb0-2583-4c80-a79e-1f756a03c0a1", "display": "Outpatient Clinic" }
 * </pre>
 * This is the single model for such references across all domains (encounter, order,
 * queue, patient, visit): the per-domain copies that used to duplicate these
 * three fields have been removed in favour of this one.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Ref implements HasUuid {
    private String uuid;      // resource identifier used in REST API
    private String display;   // human-readable name, e.g. "Vitals", "Pulse: 68"
    private List<Link> links; // HATEOAS links, returned by the server only

    public Ref(String uuid, String display) {
        this(uuid, display, null);
    }

    // Reference by uuid only, display and links stay null
    public static Ref of(String uuid) {
        return new Ref(uuid, null, null);
    }
}
