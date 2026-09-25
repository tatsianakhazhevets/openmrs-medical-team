package apiParts.models.order;

import apiParts.models.search.SearchParams;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedHashMap;
import java.util.Map;
import lombok.Builder;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderSearchParams implements SearchParams {

    private String patient;
    private String careSetting;
    private String type;
    private Integer limit;
    private Integer startIndex;
    private String representation;

    /**
     * Converts the non-null fields into query parameters for SearchRequester.
     *
     * Note: careSetting goes out as "caresetting", while AdminSteps.fetchMedications sends
     * "careSetting". Which spelling the server honours has not been verified yet.
     */
    @Override
    public Map<String, Object> toQueryParams() {
        Map<String, Object> queryParams = new LinkedHashMap<>();
        putIfPresent(queryParams, "patient", patient);
        putIfPresent(queryParams, "caresetting", careSetting);
        putIfPresent(queryParams, "t", type);
        putIfPresent(queryParams, "limit", limit);
        putIfPresent(queryParams, "startIndex", startIndex);
        putIfPresent(queryParams, "v", representation);
        return queryParams;
    }

    private static void putIfPresent(Map<String, Object> queryParams, String name, Object value) {
        if (value != null) {
            queryParams.put(name, value);
        }
    }
}