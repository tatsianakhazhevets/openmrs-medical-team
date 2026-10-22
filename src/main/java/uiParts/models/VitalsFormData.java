package uiParts.models;

import apiParts.generators.GeneratingRule;
import apiParts.models.BaseModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// Values for the "Record vitals and biometrics" form.
// Ranges are inside lowNormal / hiNormal of the obs reference range
// (GET /obs?patient={uuid}&v=full -> referenceRange), so the tables show the values without abnormal flags.
// Weight and height are narrowed so that the computed BMI stays in its normal range (18.5..24.9).
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VitalsFormData extends BaseModel {
    @GeneratingRule(min = 36.0, max = 37.5, scale = 1)
    private Double temperature;          // DEG C

    @GeneratingRule(min = 100, max = 120)
    private Integer systolic;            // mmHg

    @GeneratingRule(min = 60, max = 80)
    private Integer diastolic;           // mmHg

    @GeneratingRule(min = 60, max = 100)
    private Integer pulse;               // beats/min

    @GeneratingRule(min = 12, max = 18)
    private Integer respiratoryRate;     // breaths/min

    @GeneratingRule(min = 95, max = 100)
    private Integer oxygenSaturation;    // %

    @GeneratingRule(min = 62, max = 70, scale = 1)
    private Double weight;               // kg

    @GeneratingRule(min = 168, max = 180, scale = 1)
    private Double height;               // cm

    @GeneratingRule(min = 20, max = 35)
    private Integer midUpperArmCircumference; // cm

    @GeneratingRule(regex = "[A-Za-z]{10,30}")
    private String notes;

    // BMI is computed by the form (read-only field): weight / (height in m)^2, 1 decimal place
    public double calculateBmi() {
        double heightInMeters = height / 100;
        return Math.round(weight / (heightInMeters * heightInMeters) * 10) / 10.0;
    }
}
