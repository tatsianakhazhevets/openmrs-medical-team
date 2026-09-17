package apiParts.models.order;

import apiParts.models.BaseModel;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// POST /order: action=DISCONTINUE stops previousOrder (testorder) and records this order instead.
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DiscontinueOrderRequest extends BaseModel {
    @Builder.Default private String type = "testorder";
    @Builder.Default private String action = "DISCONTINUE";

    private String previousOrder;  // uuid of the testorder being discontinued
    private CareSetting careSetting;
    private String encounter;      // uuid of the encounter the previousOrder belongs to
    private String patient;
    private LabTestConcept concept;
    private String orderer;
}
