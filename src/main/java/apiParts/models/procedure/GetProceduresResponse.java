package apiParts.models.procedure;

import apiParts.models.BaseModel;
import apiParts.models.search.SearchResponse;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response of GET /ws/rest/v1/procedure?patient={patientUuid}&v=full.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class GetProceduresResponse extends BaseModel implements SearchResponse<ProcedureResponse> {
    private List<ProcedureResponse> results;
}
