package apiParts.models.order;

import apiParts.generators.*;
import apiParts.models.BaseModel;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.experimental.FieldNameConstants;
import lombok.*;

/**
 * Drug order inside CreateEncounterRequest.orders.
 * <p>
 * RandomModelGenerator produces a valid outpatient simple-dosing order. Reference fields that must
 * agree with the drug form are constrained by their generation rules; optional fields are omitted.
 */
@Data
@FieldNameConstants   // Fields.<name> - field names for overrides, case names, generating rules
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DrugOrder extends BaseModel {
    public static final String SIMPLE_DOSING = "org.openmrs.SimpleDosingInstructions";      // dose, doseUnits, route, frequency required
    public static final String FREE_TEXT_DOSING = "org.openmrs.FreeTextDosingInstructions"; // dosingInstructions required
    public static final String URGENCY_ROUTINE = "ROUTINE";                    // server default when urgency is omitted (Active Medications)
    public static final String URGENCY_ON_SCHEDULED_DATE = "ON_SCHEDULED_DATE"; // sent together with scheduledDate (Upcoming Medications)
    public static final int DOSE_SCALE = 1;   // decimal places of dose: generated and used in tests
    public static final String ORDER_TYPE_UUID = "131168f4-15f5-102d-96e4-000c29c2a5d7"; // "Drug Order", GET /order?orderTypes={uuid}

    @GeneratingRule(fixedValue = "drugorder")
    @Builder.Default private String type = "drugorder";
    @GeneratingRule(fixedValue = "NEW")
    @Builder.Default private String action = "NEW";
    @GeneratingRule(fixedValue = SIMPLE_DOSING)
    @Builder.Default private String dosingType = SIMPLE_DOSING;

    @GeneratingRule(strategy = GenerationStrategy.PATIENT_UUID)
    private String patient;             // patient uuid, created in test setup
    @GeneratingRule(enumClass = CareSetting.class, enumValue = "OUTPATIENT")   // outpatient rules are checked in negative cases
    private CareSetting careSetting;
    @GeneratingRule(strategy = GenerationStrategy.PROVIDER_UUID)
    private String orderer;             // provider uuid, AdminSteps.getCurrentProviderUuid()
    @GeneratingRule(enumClass = Drug.class, excludedEnumValues = "ACYCLOVIR_CREAM_3")
    private Drug drug;
    @GeneratingRule(
            strategy = GenerationStrategy.DEPENDS_ON_FIELD,
            sourceField = Fields.drug,
            valueMethod = "getConceptUuid"
    )
    private String concept;             // concept of the drug, filled by builder.drug(...) / setDrug(...)
    @GeneratingRule(min = 0.5, max = 4.0, scale = DOSE_SCALE)
    private Double dose;
    @GeneratingRule(enumClass = DosingUnit.class, enumValue = "TABLET")      // BOTTLE is not allowed for dose
    private DosingUnit doseUnits;
    @GeneratingRule(enumClass = DrugRoute.class, enumValue = "ORAL")
    private DrugRoute route;
    @GeneratingRule(enumClass = OrderFrequency.class)
    private OrderFrequency frequency;
    @GeneratingRule(booleanValue = BooleanGeneration.FALSE)
    private Boolean asNeeded;
    @GeneratingRule(min = 0, max = 5)
    private Integer numRefills;
    @GeneratingRule(min = 1, max = 30)
    private Double quantity;
    @GeneratingRule(enumClass = DosingUnit.class, enumValue = "TABLET")
    private DosingUnit quantityUnits;
    @GeneratingRule(nullable = true)    // optional; with it server calculates autoExpireDate
    private Integer duration;
    @GeneratingRule(nullable = true)
    private DurationUnit durationUnits;
    @GeneratingRule(nullable = true)
    private String dosingInstructions;  // for FREE_TEXT_DOSING
    @GeneratingRule(nullable = true)
    private String orderReasonNonCoded;
    @GeneratingRule(nullable = true)
    private String dateActivated;
    @GeneratingRule(nullable = true)
    private String urgency;             // ON_SCHEDULED_DATE, sent together with scheduledDate (Upcoming Medications)
    @GeneratingRule(nullable = true)
    private String scheduledDate;       // ISO-8601 with offset, e.g. "2026-09-19T00:00:00-04:00"

    // Lombok keeps this setter instead of generating its own: drug and its concept always go together
    public void setDrug(Drug drug) {
        this.drug = drug;
        this.concept = drug == null ? null : drug.getConceptUuid();
    }

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
