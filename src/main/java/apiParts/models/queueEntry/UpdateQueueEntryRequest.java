package apiParts.models.queueEntry;

import apiParts.models.BaseModel;
import apiParts.models.encounter.Ref;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateQueueEntryRequest extends BaseModel{
    private Ref status;
    private Ref priority;
    private String priorityComment;
}
