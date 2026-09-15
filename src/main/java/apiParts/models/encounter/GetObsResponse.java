package apiParts.models.encounter;

import apiParts.models.BaseModel;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response of GET /ws/rest/v1/obs?patient={patientUuid}.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class GetObsResponse extends BaseModel {
    private List<ObsResponse> results;
}
