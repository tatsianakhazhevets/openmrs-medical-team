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
public class OrderDeleteParams {
    private String reason;
    private Boolean purge;

    public Map<String, Object> toQueryParams() {
        Map<String, Object> queryParams = new LinkedHashMap<>();
        if (reason != null) {
            queryParams.put("reason", reason);
        }
        if (purge != null) {
            queryParams.put("purge", purge);
        }
        return queryParams;
    }
}
