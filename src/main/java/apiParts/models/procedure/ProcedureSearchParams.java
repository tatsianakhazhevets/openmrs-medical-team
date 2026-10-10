package apiParts.models.procedure;

import apiParts.models.search.SearchParams;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Filters of GET /procedure. Patient is the only supported search criterion:
 * a request without it answers 400, which is what
 * GetProcedureApiTests#adminCannotGetProceduresWithoutPatient asserts.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcedureSearchParams implements SearchParams {

    private String patient;
    private String representation;
    private Boolean includeAll;

    @Override
    public Map<String, Object> toQueryParams() {
        Map<String, Object> queryParams = new LinkedHashMap<>();
        SearchParams.putIfPresent(queryParams, "patient", patient);
        SearchParams.putIfPresent(queryParams, "v", representation);
        SearchParams.putIfPresent(queryParams, "includeAll", includeAll);
        return queryParams;
    }
}
