package apiParts.models.encounter;

import apiParts.models.search.SearchParams;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Filters of GET /obs.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ObsSearchParams implements SearchParams {

    private String patient;
    private String representation;

    @Override
    public Map<String, Object> toQueryParams() {
        Map<String, Object> queryParams = new LinkedHashMap<>();
        SearchParams.putIfPresent(queryParams, "patient", patient);
        SearchParams.putIfPresent(queryParams, "v", representation);
        return queryParams;
    }
}
