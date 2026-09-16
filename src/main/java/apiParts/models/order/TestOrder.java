package apiParts.models.order;

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
    @Builder.Default private String type = "testorder";
    @Builder.Default private String action = "NEW";

    private String patient;             // patient uuid, created in test setup
    private CareSetting careSetting;
    private String orderer;             // provider uuid, AdminSteps.getCurrentProviderUuid()
    private LabTestConcept concept;
    private String instructions;
    private String accessionNumber;
    @Builder.Default private String urgency = "ROUTINE";
    private String scheduledDate;
}
