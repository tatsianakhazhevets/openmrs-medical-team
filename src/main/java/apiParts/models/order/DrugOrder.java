package apiParts.models.order;

import apiParts.generators.*;
import apiParts.models.BaseModel;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

/**
 * Drug order inside CreateEncounterRequest.orders.
 * <p>
 * RandomModelGenerator gives valid outpatient order with simple dosing: fixed fields are the ones
 * negative cases depend on (careSetting, doseUnits), references with any valid value are random
 * (drug, route, frequency, quantityUnits, asNeeded), optional fields are not generated.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DrugOrder extends BaseModel {
    public static final String SIMPLE_DOSING = "org.openmrs.SimpleDosingInstructions";      // dose, doseUnits, route, frequency required
    public static final String FREE_TEXT_DOSING = "org.openmrs.FreeTextDosingInstructions"; // dosingInstructions required
    public static final String URGENCY_ROUTINE = "ROUTINE";                    // server default when urgency is omitted (Active Medications)
    public static final String URGENCY_ON_SCHEDULED_DATE = "ON_SCHEDULED_DATE"; // sent together with scheduledDate (Upcoming Medications)
    public static final String ORDER_TYPE_UUID = "131168f4-15f5-102d-96e4-000c29c2a5d7"; // "Drug Order", GET /order?orderTypes={uuid}

    @FixedStringGeneratingRule("drugorder")
    @Builder.Default private String type = "drugorder";
    @FixedStringGeneratingRule("NEW")
    @Builder.Default private String action = "NEW";
    @FixedStringGeneratingRule(SIMPLE_DOSING)
    @Builder.Default private String dosingType = SIMPLE_DOSING;

    @PatientUuidGeneratingRule
    private String patient;             // patient uuid, created in test setup
    @FixedEnumGeneratingRule(enumClass = CareSetting.class, value = "OUTPATIENT")   // outpatient rules are checked in negative cases
    private CareSetting careSetting;
    @ProviderUuidGeneratingRule
    private String orderer;             // provider uuid, AdminSteps.getCurrentProviderUuid()
    @EnumGeneratingRule(enumClass = Drug.class)            // any: server does not validate drug against dosing
    private Drug drug;
    @DependsOnFieldGeneratingRule(field = "drug", valueMethod = "getConceptUuid")
    private String concept;             // concept of the drug, filled by builder.drug(...) / setDrug(...)
    @DoubleGeneratingRule(min = 0.5, max = 4.0, range = 1)
    private Double dose;
    @FixedEnumGeneratingRule(enumClass = DosingUnit.class, value = "TABLET")      // BOTTLE is not allowed for dose
    private DosingUnit doseUnits;
    @EnumGeneratingRule(enumClass = DrugRoute.class)       // any: server does not validate route against drug
    private DrugRoute route;
    @EnumGeneratingRule(enumClass = OrderFrequency.class)
    private OrderFrequency frequency;
    @BooleanGeneratingRule(false)
    private Boolean asNeeded;
    @IntegerGeneratingRule(min = 0, max = 5)
    private Integer numRefills;
    @DoubleGeneratingRule(min = 1, max = 30, range = 0)
    private Double quantity;
    @EnumGeneratingRule(enumClass = DosingUnit.class)      // TABLET and BOTTLE are both valid for quantity
    private DosingUnit quantityUnits;
    @IgnoreGeneratingRule               // optional; with it server calculates autoExpireDate
    private Integer duration;
    @IgnoreGeneratingRule
    private DurationUnit durationUnits;
    @IgnoreGeneratingRule
    private String dosingInstructions;  // for FREE_TEXT_DOSING
    @IgnoreGeneratingRule
    private String orderReasonNonCoded;
    @IgnoreGeneratingRule
    private String dateActivated;
    @IgnoreGeneratingRule
    private String urgency;             // ON_SCHEDULED_DATE, sent together with scheduledDate (Upcoming Medications)
    @IgnoreGeneratingRule
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
