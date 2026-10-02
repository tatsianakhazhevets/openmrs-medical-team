package apiParts.models.order;

import apiParts.generators.GeneratingRule;
import apiParts.generators.GenerationStrategy;
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
    @GeneratingRule(fixedValue = "testorder")
    @Builder.Default private String type = "testorder";

    @GeneratingRule(fixedValue = "DISCONTINUE")
    @Builder.Default private String action = "DISCONTINUE";

    @GeneratingRule(nullable = true)
    private String previousOrder;  // uuid of the testorder being discontinued

    @GeneratingRule(property = "test_order_care_setting")
    private CareSetting careSetting;

    @GeneratingRule(nullable = true)
    private String encounter;      // uuid of the encounter the previousOrder belongs to

    @GeneratingRule(strategy = GenerationStrategy.PATIENT_UUID)
    private String patient;

    @GeneratingRule(property = "test_lab_concept")
    private LabTestConcept concept;

    @GeneratingRule(strategy = GenerationStrategy.PROVIDER_UUID)
    private String orderer;
}
