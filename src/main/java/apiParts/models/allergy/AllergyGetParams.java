package apiParts.models.allergy;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Stack;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AllergyGetParams {

    public static final String FULL = "full";

    private String v;

    public Map<String, Object> toQueryParams() {
        Map<String, Object> queryParams = new LinkedHashMap<>();

        if (v != null) {
            queryParams.put("v", v);
        }

        return queryParams;
    }
}