package apiParts.models.queueEntry;

import apiParts.models.search.SearchParams;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Filters of GET /queue-entry.
 *
 * The representation here is a long custom:(...) projection. Keeping it in a
 * params object rather than inline in a Map keeps the call site readable and
 * lets the projection be reused instead of retyped.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueueEntrySearchParams implements SearchParams {

    /** Projection used when listing active queue entries. */
    public static final String ACTIVE_ENTRY_REPRESENTATION =
            "custom:(uuid,queue:(uuid,display),status:(uuid,display),patient:(uuid,display),"
                    + "visit:(uuid,display),priority:(uuid,display),sortWeight,startedAt,endedAt)";

    private String representation;
    private String location;
    private Boolean isEnded;

    @Override
    public Map<String, Object> toQueryParams() {
        Map<String, Object> queryParams = new LinkedHashMap<>();
        SearchParams.putIfPresent(queryParams, "v", representation);
        SearchParams.putIfPresent(queryParams, "location", location);
        SearchParams.putIfPresent(queryParams, "isEnded", isEnded);
        return queryParams;
    }
}
