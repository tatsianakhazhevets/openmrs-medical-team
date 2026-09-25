package apiParts.models.order;

import apiParts.models.BaseModel;
import apiParts.models.search.SearchResponse;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class ListOrdersResponse extends BaseModel implements SearchResponse<Order> {

    private List<Order> results;
    private List<Link> links;
}