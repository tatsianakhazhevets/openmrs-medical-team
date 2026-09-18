package apiParts.testdata;

import apiParts.generators.RandomModelGenerator;
import apiParts.models.EncounterType;
import apiParts.models.Location;
import apiParts.models.encounter.CreateEncounterRequest;
import apiParts.models.order.CareSetting;
import apiParts.models.order.DosingUnit;
import apiParts.models.order.Drug;
import apiParts.models.order.DrugOrder;
import apiParts.models.order.DrugRoute;
import apiParts.models.order.LabTestConcept;
import apiParts.models.order.OrderFrequency;
import apiParts.models.order.TestOrder;

import java.util.List;

/**
 * Requests of the standard order fixtures (see apiParts.steps.AdminSteps).
 * <p>
 * Scalars are generated, references (drug, units, route, frequency) stay explicit.
 * Generated values are fixed for the whole run, so a request built here is equal to the one
 * that was actually sent and tests can build the expected model from it
 * (see apiParts.assertions.OrderAssertions).
 */
public class OrderTestData {

    // Bounds of the generated values: anything inside them is valid for the server
    private static final double MIN_DOSE = 0.5;
    private static final double MAX_DOSE = 4.0;
    private static final int DOSE_SCALE = 1;
    private static final double MIN_QUANTITY = 1;
    private static final double MAX_QUANTITY = 30;
    private static final int MAX_REFILLS = 5;
    private static final int MAX_ACCESSION_NUMBER = 9999;

    public static final double DRUG_ORDER_DOSE = RandomModelGenerator.randomDouble(MIN_DOSE, MAX_DOSE, DOSE_SCALE);
    public static final double DRUG_ORDER_QUANTITY = RandomModelGenerator.randomDouble(MIN_QUANTITY, MAX_QUANTITY, 0);
    public static final int DRUG_ORDER_NUM_REFILLS = RandomModelGenerator.randomInt(0, MAX_REFILLS);
    private static final String LAB_ORDER_INSTRUCTIONS = RandomModelGenerator.randomSentence();
    private static final String LAB_ORDER_ACCESSION_NUMBER =
            String.valueOf(RandomModelGenerator.randomInt(1, MAX_ACCESSION_NUMBER));

    private OrderTestData() {
    }

    // Valid outpatient drug order with simple dosing (Aspirin): baseline for order fixtures
    // and for tests that change one field at a time
    public static DrugOrder.DrugOrderBuilder validOutpatientDrugOrder(String patientUUID, String ordererUUID) {
        return DrugOrder.builder()
                .patient(patientUUID)
                .careSetting(CareSetting.OUTPATIENT)
                .orderer(ordererUUID)
                .drug(Drug.ASPIRIN_325MG)
                .dose(DRUG_ORDER_DOSE)
                .doseUnits(DosingUnit.TABLET)
                .route(DrugRoute.ORAL)
                .frequency(OrderFrequency.ONCE_DAILY)
                .quantity(DRUG_ORDER_QUANTITY)
                .quantityUnits(DosingUnit.TABLET)
                .numRefills(DRUG_ORDER_NUM_REFILLS);
    }

    // Encounter with one valid drug order
    public static CreateEncounterRequest drugOrderEncounterRequest(String patientUUID, String ordererUUID) {
        return CreateEncounterRequest.builder()
                .patient(patientUUID)
                .encounterType(EncounterType.ORDER)
                .location(Location.OUTPATIENT_CLINIC)
                .orders(List.of(validOutpatientDrugOrder(patientUUID, ordererUUID).build()))
                .build();
    }

    // Valid lab order (Alkaline phosphatase test)
    public static TestOrder validLabOrder(String patientUUID, String ordererUUID) {
        return TestOrder.builder()
                .patient(patientUUID)
                .careSetting(CareSetting.OUTPATIENT)
                .orderer(ordererUUID)
                .concept(LabTestConcept.ALKALINE_PHOSPHATASE)
                .instructions(LAB_ORDER_INSTRUCTIONS)
                .accessionNumber(LAB_ORDER_ACCESSION_NUMBER)
                .build();
    }

    // Encounter with one valid lab order
    public static CreateEncounterRequest labOrderEncounterRequest(String patientUUID, String ordererUUID) {
        return CreateEncounterRequest.builder()
                .patient(patientUUID)
                .encounterType(EncounterType.ORDER)
                .location(Location.INPATIENT_WARD)
                .orders(List.of(validLabOrder(patientUUID, ordererUUID)))
                .build();
    }
}
