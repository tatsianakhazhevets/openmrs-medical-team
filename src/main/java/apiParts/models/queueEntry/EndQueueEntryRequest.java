package apiParts.models.queueEntry;

import apiParts.models.BaseModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EndQueueEntryRequest extends BaseModel {
    private String endedAt;
}
