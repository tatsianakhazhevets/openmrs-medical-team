package apiParts.models.queueEntry;

import apiParts.models.BaseModel;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GetQueueEntryResponse extends BaseModel {
    private List<QueueEntryResponse> results;
    private Integer totalCount;
}
