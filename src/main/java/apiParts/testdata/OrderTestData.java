package apiParts.testdata;

import apiParts.generators.RandomModelGenerator;
import apiParts.models.Location;
import apiParts.models.encounter.CreateEncounterRequest;
import apiParts.models.encounter.EncounterType;
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
import java.util.HashMap;
import java.util.Map;

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

    // Upcoming Medications fixture: scheduled this many days ahead of now
    private static final int SCHEDULED_DAYS_AHEAD = 2;
    // Reason sent with DISCONTINUE drug orders; server requires a non-empty value, content is irrelevant
    private static final String DISCONTINUE_REASON = "test indication";

    // dose/quantity/numRefills of the standard drug order fixture, generated once (rules in DrugOrder)
    // and reused for the whole run: validOutpatientDrugOrder(...) is called once to actually create the
    // order and again by tests to build the expected model, both calls must produce the same values.
    // patient/orderer/drug are overridden here with placeholders so this static generation does not
    // depend on any session/patient state or make network calls.
    private static final DrugOrder FIXTURE_SCALARS = RandomModelGenerator.generate(
            DrugOrder.class,
            Map.of(
                    "patient", "placeholder-patient",
                    "orderer", "placeholder-orderer",
                    "drug", Drug.ASPIRIN_325MG
            )
    );

    private OrderTestData() {
    }

    // Valid outpatient drug order with simple dosing (Aspirin): baseline for order fixtures
    // and for tests that change one field at a time
    public static DrugOrder validOutpatientDrugOrder(String patientUUID, String ordererUUID) {
        return RandomModelGenerator.generate(
                DrugOrder.class,
                Map.of(
                        "patient", patientUUID,
                        "orderer", ordererUUID,
                        "drug", Drug.ASPIRIN_325MG,
                        "doseUnits", DosingUnit.TABLET,
                        "route", DrugRoute.ORAL,
                        "frequency", OrderFrequency.ONCE_DAILY,
                        "quantityUnits", DosingUnit.TABLET,
                        "dose", FIXTURE_SCALARS.getDose(),
                        "quantity", FIXTURE_SCALARS.getQuantity(),
                        "numRefills", FIXTURE_SCALARS.getNumRefills()
                )
        );
    }

    // Encounter with one valid drug order
    public static CreateEncounterRequest drugOrderEncounterRequest(String patientUUID, String ordererUUID) {
        return orderEncounterRequest(
                patientUUID,
                Location.OUTPATIENT_CLINIC,
                validOutpatientDrugOrder(patientUUID, ordererUUID)
        );
    }

    // Valid lab order (Alkaline phosphatase test)
    public static TestOrder validLabOrder(String patientUUID, String ordererUUID) {
        return RandomModelGenerator.generate(
                TestOrder.class,
                Map.of("patient", patientUUID, "orderer", ordererUUID)
        );
    }

    // Encounter with one valid lab order
    public static CreateEncounterRequest labOrderEncounterRequest(String patientUUID, String ordererUUID) {
        return orderEncounterRequest(
                patientUUID,
                Location.INPATIENT_WARD,
                validLabOrder(patientUUID, ordererUUID)
        );
    }

    // Lab order with concept intentionally omitted - concept is required for a test order
    public static TestOrder labOrderWithoutConcept(String patientUUID, String ordererUUID) {
        TestOrder order = validLabOrder(patientUUID, ordererUUID);
        order.setCareSetting(CareSetting.INPATIENT);
        order.setConcept(null);
        return order;
    }

    // Encounter with one lab order missing the required concept field
    public static CreateEncounterRequest labOrderRequestWithoutConcept(String patientUUID, String ordererUUID) {
        return orderEncounterRequest(
                patientUUID,
                Location.INPATIENT_WARD,
                labOrderWithoutConcept(patientUUID, ordererUUID)
        );
    }

    // Lab order with careSetting intentionally omitted - careSetting is required for any order
    public static TestOrder labOrderWithoutCareSetting(String patientUUID, String ordererUUID) {
        TestOrder order = validLabOrder(patientUUID, ordererUUID);
        order.setCareSetting(null);
        return order;
    }

    // Encounter with one lab order missing the required careSetting field
    public static CreateEncounterRequest labOrderRequestWithoutCareSetting(String patientUUID, String ordererUUID) {
        return orderEncounterRequest(
                patientUUID,
                Location.INPATIENT_WARD,
                labOrderWithoutCareSetting(patientUUID, ordererUUID)
        );
    }

    // Same standard outpatient drug order as drugOrderEncounterRequest, but scheduled ahead
    // (urgency=ON_SCHEDULED_DATE) - Upcoming Medications fixture
    public static CreateEncounterRequest upcomingDrugOrderEncounterRequest(String patientUUID, String ordererUUID) {
        DrugOrder order = validOutpatientDrugOrder(patientUUID, ordererUUID);
        order.setUrgency(DrugOrder.URGENCY_ON_SCHEDULED_DATE);
        order.setScheduledDate(DateTimeUtils.nowPlusDays(SCHEDULED_DAYS_AHEAD).format(OPENMRS_REQUEST_DATE_TIME));

        return orderEncounterRequest(patientUUID, Location.OUTPATIENT_CLINIC, order);
    }

    // Discontinues a testorder (POST /order, action=DISCONTINUE), the way the laboratory
    // stops a test order once its result has been captured
    public static DiscontinueOrderRequest discontinueOrderRequest(
            String orderUUID, String patientUUID, String encounterUUID, String ordererUUID) {
        return RandomModelGenerator.generate(
                DiscontinueOrderRequest.class,
                Map.of(
                        "previousOrder", orderUUID,
                        "encounter", encounterUUID,
                        "patient", patientUUID,
                        "orderer", ordererUUID
                )
        );
    }

    // Discontinues a drug order (POST /encounter, action=DISCONTINUE), the way the Medications page
    // stops an active/upcoming medication
    public static CreateEncounterRequest discontinueDrugOrderEncounterRequest(
            String patientUUID, String orderUUID, Drug drug, String ordererUUID) {
        DiscontinueDrugOrderRequest order = RandomModelGenerator.generate(
                DiscontinueDrugOrderRequest.class,
                Map.of(
                        "previousOrder", orderUUID,
                        "patient", patientUUID,
                        "orderer", ordererUUID,
                        "drug", drug,
                        "orderReasonNonCoded", DISCONTINUE_REASON
                )
        );

        return orderEncounterRequest(patientUUID, Location.OUTPATIENT_CLINIC, order);
    }

    // POST /order/{uuid}/fulfillerdetails/ body: laboratory-side status update of a test order
    public static FulfillerDetailsRequest fulfillerDetailsRequest(FulfillerStatus status, String comment) {
        Map<String, Object> overrides = new HashMap<>();
        overrides.put("fulfillerStatus", status);
        overrides.put("fulfillerComment", comment);
        return RandomModelGenerator.generate(FulfillerDetailsRequest.class, overrides);
    }

    private static CreateEncounterRequest orderEncounterRequest(
            String patientUUID,
            Location location,
            Object order
    ) {
        return RandomModelGenerator.generate(
                CreateEncounterRequest.class,
                Map.of(
                        "patient", patientUUID,
                        "encounterType", EncounterType.ORDER,
                        "location", location,
                        "orders", List.of(order)
                )
        );
    }
}
