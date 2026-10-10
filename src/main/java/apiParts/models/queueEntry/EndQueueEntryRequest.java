package apiParts.models.queueEntry;

import apiParts.generators.GeneratingRule;
import apiParts.generators.GenerationStrategy;
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
    @GeneratingRule(strategy = GenerationStrategy.DATE_TIME)
    private String endedAt;
}
