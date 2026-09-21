package apiParts.models.queue;

import apiParts.models.search.SearchParams;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Filters of GET /queue.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueueSearchParams implements SearchParams {

    /** Projection used when looking up a queue by name and location. */
    public static final String QUEUE_LOOKUP_REPRESENTATION =
            "custom:(uuid,display,name,description,service:(uuid,display),"
                    + "allowedPriorities:(uuid,display),allowedStatuses:(uuid,display),location:(uuid,display))";

    private String representation;

    @Override
    public Map<String, Object> toQueryParams() {
        Map<String, Object> queryParams = new LinkedHashMap<>();
        SearchParams.putIfPresent(queryParams, "v", representation);
        return queryParams;
    }
}
