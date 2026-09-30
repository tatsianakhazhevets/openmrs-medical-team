package apiParts.models.order;

import apiParts.generators.GeneratingRule;
import apiParts.generators.GenerationStrategy;
import apiParts.models.BaseModel;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// Lab work order (e.g. blood test), sent as part of encounter.orders
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TestOrder extends BaseModel {
    public static final String CONCEPT_FIELD_NAME = "concept";

    @GeneratingRule(fixedValue = "testorder")
    @Builder.Default private String type = "testorder";

    @GeneratingRule(fixedValue = "NEW")
    @Builder.Default private String action = "NEW";

    @GeneratingRule(strategy = GenerationStrategy.PATIENT_UUID)
    private String patient;             // patient uuid, created in test setup

    @GeneratingRule(property = "test_order_care_setting")
    private CareSetting careSetting;

    @GeneratingRule(strategy = GenerationStrategy.PROVIDER_UUID)
    private String orderer;             // provider uuid, AdminSteps.getCurrentProviderUuid()

    @GeneratingRule(property = "test_lab_concept")
    private LabTestConcept concept;

    @GeneratingRule(regex = "[A-Za-z][A-Za-z ]{4,99}")
    private String instructions;

    @GeneratingRule(regex = "[1-9][0-9]{0,3}")
    private String accessionNumber;

    @GeneratingRule(fixedValue = "ROUTINE")
    @Builder.Default private String urgency = "ROUTINE";

    @GeneratingRule(nullable = true)
    private String scheduledDate;
}
