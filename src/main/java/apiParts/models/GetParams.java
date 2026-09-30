package apiParts.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedHashMap;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetParams {

    public static final String FULL = "full";
    public static final String DEFAULT = "default";
    private String patient;

    private String v;

    public Map<String, Object> toQueryParams() {
        Map<String, Object> queryParams = new LinkedHashMap<>();

        if (v != null) {
            queryParams.put("v", v);
        }

        if (patient != null) {
            queryParams.put("patient", patient);
        }

        return queryParams;
    }
}