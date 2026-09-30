package apiParts.models.queueEntry;

import apiParts.generators.GeneratingRule;
import apiParts.models.BaseModel;
import apiParts.models.Ref;
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
    @GeneratingRule(regex = "[A-Za-z][A-Za-z ]{4,99}")
    private String priorityComment;
}
