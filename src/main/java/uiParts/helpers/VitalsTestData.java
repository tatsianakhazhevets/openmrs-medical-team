package uiParts.helpers;

import apiParts.generators.RandomModelGenerator;
import uiParts.models.VitalsFormData;
import uiParts.models.VitalsInterpretation;

import static uiParts.models.VitalsReferenceRange.*;

/**
 * Form data for highlighting cases: generated normal model + setters for the values with reference limits.
 */
public final class VitalsTestData {
    private static final int MAX_BMI_ATTEMPTS = 100;

    private VitalsTestData() {
    }

    // every value with reference limits gets the interpretation (SpO2 only when it supports it),
    // weight / height / MUAC / notes stay as generated
    public static VitalsFormData withInterpretation(VitalsInterpretation interpretation) {
        VitalsFormData vitals = RandomModelGenerator.generate(VitalsFormData.class);
        vitals.setTemperature(TEMPERATURE.randomValue(interpretation));
        vitals.setSystolic((int) SYSTOLIC.randomValue(interpretation));
        vitals.setDiastolic((int) DIASTOLIC.randomValue(interpretation));
        vitals.setPulse((int) PULSE.randomValue(interpretation));
        vitals.setRespiratoryRate((int) RESPIRATORY_RATE.randomValue(interpretation));
        if (OXYGEN_SATURATION.supports(interpretation)) {
            vitals.setOxygenSaturation((int) OXYGEN_SATURATION.randomValue(interpretation));
        }
        setWeightForBmi(vitals, interpretation);
        return vitals;
    }

    // BMI is computed by the form from weight and height: weight is picked for a target BMI,
    // retried while rounding of weight / BMI moves the result out of the range
    private static void setWeightForBmi(VitalsFormData vitals, VitalsInterpretation interpretation) {
        for (int i = 0; i < MAX_BMI_ATTEMPTS; i++) {
            double heightInMeters = vitals.getHeight() / 100;
            double targetBmi = BMI.randomValue(interpretation);
            vitals.setWeight(Math.round(targetBmi * heightInMeters * heightInMeters * 10) / 10.0);
            if (BMI.isIn(interpretation, vitals.calculateBmi())) {
                return;
            }
        }
        throw new IllegalStateException("Could not pick weight for " + interpretation + " BMI, height " + vitals.getHeight());
    }
}
