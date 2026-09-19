package apiParts.models.queue;

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
public class GetQueueResponse extends BaseModel implements SearchResponse<QueueResponse> {

    private List<QueueResponse> results;
}
