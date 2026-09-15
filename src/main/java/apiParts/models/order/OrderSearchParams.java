package apiParts.models.order;

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
public class OrderSearchParams {

    private String patient;
    private String careSetting;
    private Integer limit;
    private Integer startIndex;
    private String representation;

    /**
     * Converts the non-null fields into query parameters accepted by
     * {@code CrudEndpoint#get(Map)}, so a single generic requester can be reused
     * instead of a dedicated endpoint/requester pair for every query-param GET.
     */
    public Map<String, Object> toQueryParams() {
        Map<String, Object> queryParams = new LinkedHashMap<>();
        putIfPresent(queryParams, "patient", patient);
        putIfPresent(queryParams, "caresetting", careSetting);
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