package apiParts.models.order;

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
    @Builder.Default private String type = "drugorder";
    @Builder.Default private String action = "DISCONTINUE";

    private String previousOrder;  // uuid of the drug order being discontinued
    private CareSetting careSetting;
    private String patient;
    private String orderer;
    private Drug drug;
    private String concept;        // filled by builder.drug(...)
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
