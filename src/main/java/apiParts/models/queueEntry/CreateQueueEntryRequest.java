package apiParts.models.queueEntry;

import apiParts.models.BaseModel;
import apiParts.models.encounter.Ref;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreateQueueEntryRequest extends BaseModel {
    private Ref visit;
    private QueueEntry queueEntry;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QueueEntry {
        private Ref status;
        private Ref priority;
        private Ref queue;
        private Ref patient;
        private String startedAt;
        private Integer sortWeight;
    }
}
