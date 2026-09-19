package apiParts.models.patient;

import apiParts.models.search.SearchParams;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Filters of GET /patient?q=... - a free-text search.
 *
 * Note the semantics this makes explicit: a query matching nobody answers
 * 200 with an empty results list, NOT 404. CrudEndpoint.get(uuid) answers 404
 * for a missing patient, which is why the two cannot share one contract.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatientSearchParams implements SearchParams {

    private String query;
    private String representation;
    private Integer limit;

    @Override
    public Map<String, Object> toQueryParams() {
        Map<String, Object> queryParams = new LinkedHashMap<>();
        SearchParams.putIfPresent(queryParams, "q", query);
        SearchParams.putIfPresent(queryParams, "v", representation);
        SearchParams.putIfPresent(queryParams, "limit", limit);
        return queryParams;
    }
}
