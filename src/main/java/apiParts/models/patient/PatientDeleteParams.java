package apiParts.models.patient;

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
public class PatientDeleteParams {
    private Boolean purge;

    public Map<String, Object> toQueryParams() {
        Map<String, Object> queryParams = new LinkedHashMap<>();

        if (purge != null) {
            queryParams.put("purge", purge);
        }

        return queryParams;
    }
}