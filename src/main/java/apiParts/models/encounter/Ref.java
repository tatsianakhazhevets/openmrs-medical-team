package apiParts.models.encounter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Lightweight reference to a related OpenMRS resource.
 * <p>
 * In the default representation OpenMRS does not embed full nested objects
 * (patient, location, visit, obs, etc.). It returns a short reference that
 * contains only the resource uuid and a human-readable display name.
 * Use the uuid to fetch the full resource via its own endpoint if needed.
 * <p>
 * Example:
 * <pre>
 * { "uuid": "44c3efb0-2583-4c80-a79e-1f756a03c0a1", "display": "Outpatient Clinic" }
 * </pre>
 */
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Ref {
    private String uuid;    // resource identifier used in REST API
    private String display; // human-readable name, e.g. "Vitals", "Pulse: 68"
}