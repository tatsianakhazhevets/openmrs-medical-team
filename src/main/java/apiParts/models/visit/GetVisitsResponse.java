package apiParts.models.visit;

import apiParts.models.BaseModel;
import apiParts.models.search.SearchResponse;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class GetVisitsResponse extends BaseModel implements SearchResponse<GetVisitResponse> {

    private List<GetVisitResponse> results;
    private Integer totalCount;
}