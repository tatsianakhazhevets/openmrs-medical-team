package apiParts.models.order;

import apiParts.models.BaseModel;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DrugOrder extends BaseModel {
    public static final String SIMPLE_DOSING = "org.openmrs.SimpleDosingInstructions";      // dose, doseUnits, route, frequency required
    public static final String FREE_TEXT_DOSING = "org.openmrs.FreeTextDosingInstructions"; // dosingInstructions required

    @Builder.Default private String type = "drugorder";
    @Builder.Default private String action = "NEW";
    @Builder.Default private String dosingType = SIMPLE_DOSING;

    private String patient;             // patient uuid, created in test setup
    private CareSetting careSetting;
    private String orderer;             // provider uuid, AdminSteps.getCurrentProviderUuid()
    private Drug drug;
    private String concept;             // filled by builder.drug(...)
    private Double dose;
    private DosingUnit doseUnits;
    private DrugRoute route;
    private OrderFrequency frequency;
    private Boolean asNeeded;
    private Integer numRefills;
    private Double quantity;
    private DosingUnit quantityUnits;
    private Integer duration;
    private DurationUnit durationUnits;
    private String dosingInstructions;  // for FREE_TEXT_DOSING
    private String orderReasonNonCoded;
    private String dateActivated;

    // Lombok keeps this method instead of generating its own:
    // drug and its concept always go together
    public static class DrugOrderBuilder {
        public DrugOrderBuilder drug(Drug drug) {
            this.drug = drug;
            this.concept = drug.getConceptUuid();
            return this;
        }
    }
}
