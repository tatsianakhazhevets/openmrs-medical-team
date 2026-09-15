package apiParts.models.encounter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class ObsResponse {
    private String uuid;
    private String display;     // e.g. "Pulse: 68.0"
    private Ref concept;
    private Ref person;         // patient
    private Ref encounter;
    private Object value;       // Double for NUMERIC, String for TEXT
    private Boolean voided;

    // Server writes numeric value inconsistently: 100.0 but 0 -> Jackson reads Double or Integer.
    // Normalize any number to Double on deserialization, so 0 and 0.0 are equal in comparison.
    public void setValue(Object value) {
        this.value = value instanceof Number number ? number.doubleValue() : value;
    }
}
