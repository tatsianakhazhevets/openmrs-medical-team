package apiParts.testdata;

import java.util.Map;
import apiParts.generators.GenerationProfile;
import apiParts.generators.RandomModelGenerator;
import apiParts.models.encounter.CreateEncounterRequest;
import apiParts.models.order.CareSetting;
import apiParts.models.order.DiscontinueDrugOrderRequest;
import apiParts.models.order.DiscontinueOrderRequest;
import apiParts.models.order.DosingUnit;
import apiParts.models.order.Drug;
import apiParts.models.order.DrugOrder;
import apiParts.models.order.DrugRoute;
import apiParts.models.order.FulfillerDetailsRequest;
import apiParts.models.order.FulfillerStatus;
import apiParts.models.order.LabTestConcept;
import apiParts.models.order.OrderFrequency;
import apiParts.models.order.TestOrder;
import apiParts.utils.DateTimeUtils;

import java.util.List;

import static apiParts.utils.DateTimeUtils.OPENMRS_REQUEST_DATE_TIME;

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

    // Upcoming Medications fixture: scheduled this many days ahead of now
    private static final int SCHEDULED_DAYS_AHEAD = 2;
    // Reason sent with DISCONTINUE drug orders; server requires a non-empty value, content is irrelevant
    private static final String DISCONTINUE_REASON = "test indication";

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
        return orderEncounterRequest(patientUUID, List.of(validOutpatientDrugOrder(patientUUID, ordererUUID).build()));
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
        return orderEncounterRequest(patientUUID, List.of(validLabOrder(patientUUID, ordererUUID)));
    }

    // Same standard outpatient drug order as drugOrderEncounterRequest, but scheduled ahead
    // (urgency=ON_SCHEDULED_DATE) - Upcoming Medications fixture
    public static CreateEncounterRequest upcomingDrugOrderEncounterRequest(String patientUUID, String ordererUUID) {
        DrugOrder order = validOutpatientDrugOrder(patientUUID, ordererUUID)
                .urgency(DrugOrder.URGENCY_ON_SCHEDULED_DATE)
                .scheduledDate(DateTimeUtils.nowPlusDays(SCHEDULED_DAYS_AHEAD).format(OPENMRS_REQUEST_DATE_TIME))
                .build();

        return orderEncounterRequest(patientUUID, List.of(order));
    }

    // Discontinues a testorder (POST /order, action=DISCONTINUE), the way the laboratory
    // stops a test order once its result has been captured
    public static DiscontinueOrderRequest discontinueOrderRequest(
            String orderUUID, String patientUUID, String encounterUUID, String ordererUUID) {
        return DiscontinueOrderRequest.builder()
                .previousOrder(orderUUID)
                .careSetting(CareSetting.OUTPATIENT)
                .encounter(encounterUUID)
                .patient(patientUUID)
                .concept(LabTestConcept.ALKALINE_PHOSPHATASE)
                .orderer(ordererUUID)
                .build();
    }

    // Discontinues a drug order (POST /encounter, action=DISCONTINUE), the way the Medications page
    // stops an active/upcoming medication
    public static CreateEncounterRequest discontinueDrugOrderEncounterRequest(
            String patientUUID, String orderUUID, Drug drug, String ordererUUID) {
        DiscontinueDrugOrderRequest order = DiscontinueDrugOrderRequest.builder()
                .previousOrder(orderUUID)
                .careSetting(CareSetting.OUTPATIENT)
                .patient(patientUUID)
                .orderer(ordererUUID)
                .drug(drug)
                .orderReasonNonCoded(DISCONTINUE_REASON)
                .build();

        return orderEncounterRequest(patientUUID, List.of(order));
    }

    // Encounter of type ORDER with the given orders (patient of the encounter must be the patient of the orders)
    public static CreateEncounterRequest orderEncounterRequest(String patientUUID, List<?> orders) {
        return RandomModelGenerator.generate(CreateEncounterRequest.class, GenerationProfile.ORDER,
                Map.of("patient", patientUUID, "orders", orders));
    }

    // POST /order/{uuid}/fulfillerdetails/ body: laboratory-side status update of a test order
    public static FulfillerDetailsRequest fulfillerDetailsRequest(FulfillerStatus status, String comment) {
        return FulfillerDetailsRequest.builder()
                .fulfillerStatus(status)
                .fulfillerComment(comment)
                .build();
    }
}
