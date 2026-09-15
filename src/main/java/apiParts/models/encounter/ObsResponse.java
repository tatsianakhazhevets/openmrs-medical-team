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
}
