package apiParts.models.order;

import apiParts.generators.GeneratingRule;
import apiParts.generators.GenerationStrategy;
import apiParts.models.BaseModel;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// POST /encounter: an order with action=DISCONTINUE stops previousOrder (drugorder) and is recorded
// as a new order in its place; GET /order?excludeDiscontinueOrders=true then hides this stub order
// and returns previousOrder with dateStopped set instead (Past Medications).
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DiscontinueDrugOrderRequest extends BaseModel {
    @GeneratingRule(fixedValue = "drugorder")
    @Builder.Default private String type = "drugorder";

    @GeneratingRule(fixedValue = "DISCONTINUE")
    @Builder.Default private String action = "DISCONTINUE";

    @GeneratingRule(nullable = true)
    private String previousOrder;  // uuid of the drug order being discontinued

    @GeneratingRule(property = "test_order_care_setting")
    private CareSetting careSetting;

    @GeneratingRule(strategy = GenerationStrategy.PATIENT_UUID)
    private String patient;

    @GeneratingRule(strategy = GenerationStrategy.PROVIDER_UUID)
    private String orderer;

    private Drug drug;

    @GeneratingRule(
            strategy = GenerationStrategy.DEPENDS_ON_FIELD,
            sourceField = "drug",
            valueMethod = "getConceptUuid"
    )
    private String concept;        // filled by builder.drug(...)

    @GeneratingRule(regex = "[A-Za-z][A-Za-z ]{4,99}")
    private String orderReasonNonCoded;

    // Lombok keeps this method instead of generating its own:
    // drug and its concept always go together
    public static class DiscontinueDrugOrderRequestBuilder {
        public DiscontinueDrugOrderRequestBuilder drug(Drug drug) {
            this.drug = drug;
            this.concept = drug.getConceptUuid();
            return this;
        }
    }
}
