package apiParts.models.queueEntry;

import apiParts.models.BaseModel;
import apiParts.models.encounter.Ref;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateQueueEntryResponse extends BaseModel {
    private String uuid;
    private Ref status;
    private Ref patient;
    private Ref visit;
    private Ref priority;
    private String priorityComment;
    private Double sortWeight;
    private String startedAt;
    private String endedAt;
}
