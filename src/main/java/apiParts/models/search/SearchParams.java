package apiParts.models.search;

import java.util.Map;

/**
 * Typed query parameters of a search endpoint.
 *
 * Replaces raw Map.of("patient", uuid, "v", "full") scattered across tests:
 * a typo in a key name is caught by the compiler instead of producing
 * a silently different request.
 */
public interface SearchParams {

    Map<String, Object> toQueryParams();

    /**
     * Shared helper: only non-null filters end up in the request, so one params
     * class can serve calls that set different subsets of the filters.
     */
    static void putIfPresent(Map<String, Object> queryParams, String name, Object value) {
        if (value != null) {
            queryParams.put(name, value);
        }
    }
}
