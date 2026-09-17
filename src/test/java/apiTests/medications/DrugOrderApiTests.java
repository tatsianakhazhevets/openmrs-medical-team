package apiTests.medications;

import apiParts.assertions.ModelAssertions;
import apiParts.assertions.OrderAssertions;
import apiParts.models.EncounterType;
import apiParts.models.Location;
import apiParts.models.encounter.CreateEncounterRequest;
import apiParts.models.encounter.CreateEncounterResponse;
import apiParts.models.errors.DrugOrderFieldError;
import apiParts.models.errors.OrderErrorMessage;
import apiParts.models.order.CareSetting;
import apiParts.models.order.DosingUnit;
import apiParts.models.order.Drug;
import apiParts.models.order.DrugOrder;
import apiParts.models.order.DrugOrder.DrugOrderBuilder;
import apiParts.models.order.DrugRoute;
import apiParts.models.order.DurationUnit;
import apiParts.models.order.GetOrderResponse;
import apiParts.models.order.OrderFrequency;
import apiParts.skelethon.endpoints.Endpoint;
import apiParts.skelethon.requests.common.CrudRequester;
import apiParts.skelethon.requests.common.SuccessfulCrudRequester;
import apiParts.specs.RequestSpecs;
import apiParts.specs.ResponseSpecs;
import apiParts.steps.AdminSteps;
import apiTests.BaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAmount;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import static apiParts.models.errors.DrugOrderFieldError.*;
import static org.assertj.core.api.Assertions.assertThat;

public class DrugOrderApiTests extends BaseTest {
    private static final DateTimeFormatter OPENMRS_DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    private String patientUUID;
    private String ordererUUID;

    // new patient for each test and each parameter - no "duplicate active order" conflicts
    @BeforeEach
    void setUp() {
        patientUUID = AdminSteps.createPatient().getUuid();
        ordererUUID = AdminSteps.getCurrentProviderUuid();
    }

    // Each case = valid outpatient order + one change, server must save it as sent
    static Stream<Arguments> validDrugOrders() {
        return Stream.of(
                Arguments.of("valid outpatient order", mutate(b -> b)),
                Arguments.of("fractional dose = 0.5", mutate(b -> b.dose(0.5))),
                Arguments.of("quantityUnits from dispensing set", mutate(b -> b.quantityUnits(DosingUnit.BOTTLE))),
                Arguments.of("inpatient without quantity, quantityUnits, numRefills", mutate(b -> b
                        .careSetting(CareSetting.INPATIENT)
                        .quantity(null).quantityUnits(null).numRefills(null))),
                Arguments.of("duration with durationUnits", mutate(b -> b.duration(5).durationUnits(DurationUnit.DAYS))),
                Arguments.of("durationUnits without duration", mutate(b -> b.durationUnits(DurationUnit.DAYS))),
                Arguments.of("without concept - server takes it from drug", mutate(b -> b.concept(null))),
                Arguments.of("free text dosing with dosingInstructions only", mutate(b -> b
                        .dosingType(DrugOrder.FREE_TEXT_DOSING)
                        .dosingInstructions("1 tablet after meal")
                        .dose(null).doseUnits(null).route(null).frequency(null))),
                Arguments.of("asNeeded without asNeededCondition", mutate(b -> b.asNeeded(true))),

                // Server does not validate combinations of fields - current behavior is fixed here
                Arguments.of("tablet with intravenous route", mutate(b -> b.route(DrugRoute.INTRAVENOUS))),
                Arguments.of("cream drug dosed in tablets orally", mutate(b -> b.drug(Drug.ACYCLOVIR_CREAM_3)))
        );
    }

    // autoExpireDate = dateActivated + duration - 1 second (server ends order "a moment before")
    // OCCURRENCES: duration = number of doses, period = doses / frequency per day
    static Stream<Arguments> durations() {
        return Stream.of(
                Arguments.of(30, DurationUnit.SECONDS, OrderFrequency.ONCE_DAILY, Duration.ofSeconds(30)),
                Arguments.of(30, DurationUnit.MINUTES, OrderFrequency.ONCE_DAILY, Duration.ofMinutes(30)),
                Arguments.of(3, DurationUnit.HOURS, OrderFrequency.ONCE_DAILY, Duration.ofHours(3)),
                Arguments.of(5, DurationUnit.DAYS, OrderFrequency.ONCE_DAILY, Period.ofDays(5)),
                Arguments.of(2, DurationUnit.WEEKS, OrderFrequency.ONCE_DAILY, Period.ofWeeks(2)),
                Arguments.of(1, DurationUnit.MONTHS, OrderFrequency.ONCE_DAILY, Period.ofMonths(1)),
                Arguments.of(1, DurationUnit.YEARS, OrderFrequency.ONCE_DAILY, Period.ofYears(1)),
                Arguments.of(3, DurationUnit.OCCURRENCES, OrderFrequency.ONCE_DAILY, Period.ofDays(3)),
                Arguments.of(3, DurationUnit.OCCURRENCES, OrderFrequency.TWICE_DAILY, Duration.ofHours(36))
        );
    }

    // Each case = valid outpatient order + one change -> expected validation error
    static Stream<Arguments> invalidDrugOrders() {
        return Stream.of(
                // Simple dosing: required fields and dose > 0
                Arguments.of("dose = 0", mutate(b -> b.dose(0.0)), DOSE_ZERO_OR_LESS),
                Arguments.of("dose = -1", mutate(b -> b.dose(-1.0)), DOSE_ZERO_OR_LESS),
                Arguments.of("simple dosing without dose", mutate(b -> b.dose(null)), DOSE_IS_NULL_FOR_SIMPLE_DOSING),
                Arguments.of("simple dosing without route", mutate(b -> b.route(null)), ROUTE_IS_NULL_FOR_SIMPLE_DOSING),
                Arguments.of("doseUnits from dispensing set only", mutate(b -> b.doseUnits(DosingUnit.BOTTLE)),
                        DOSE_UNITS_NOT_AMONG_ALLOWED),

                // Free text dosing
                Arguments.of("free text dosing without dosingInstructions", mutate(b -> b
                                .dosingType(DrugOrder.FREE_TEXT_DOSING)
                                .dose(null).doseUnits(null).route(null).frequency(null)),
                        DOSING_INSTRUCTIONS_IS_NULL_FOR_FREE_TEXT_DOSING),

                // Outpatient (drugOrder.requireOutpatientQuantity = true)
                Arguments.of("outpatient without quantity", mutate(b -> b.quantity(null)), QUANTITY_IS_NULL_FOR_OUTPATIENT),
                Arguments.of("outpatient without quantityUnits", mutate(b -> b.quantityUnits(null)),
                        QUANTITY_UNITS_REQUIRED_WITH_QUANTITY),
                Arguments.of("outpatient without numRefills", mutate(b -> b.numRefills(null)), NUM_REFILLS_IS_NULL_FOR_OUTPATIENT),

                // Duration
                Arguments.of("duration without durationUnits", mutate(b -> b.duration(5)), DURATION_UNITS_REQUIRED_WITH_DURATION),

                // Drug and concept
                Arguments.of("concept does not match drug",
                        mutate(b -> b.concept(Drug.ACETAMINOPHEN_325MG.getConceptUuid())), CONCEPT_NOT_MATCHING_DRUG),

                // KNOWN ISSUES: server accepts these (201), test is expected to fail until fixed
                Arguments.of("[known issue] quantity = 0", mutate(b -> b.quantity(0.0)), QUANTITY_ZERO_OR_LESS),
                Arguments.of("[known issue] numRefills = -1", mutate(b -> b.numRefills(-1)), NUM_REFILLS_NEGATIVE)
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("validDrugOrders")
    public void adminCanCreateDrugOrder(String caseName, UnaryOperator<DrugOrderBuilder> mutation) {
        var request = encounterWith(mutation.apply(validOutpatientOrder()).build());

        var encounter = new SuccessfulCrudRequester<CreateEncounterResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.ENCOUNTER_POST,
                ResponseSpecs.requestReturnsCreated()
        )
                .create(request);

        var patientOrders = getPatientOrders();

        ModelAssertions.assertListMatchesExpected(softly,
                patientOrders.getResults(),
                OrderAssertions.expectedOrdersOf(request),
                OrderAssertions::drugUuidOf,
                "drug orders saved for patient");
        softly.assertThat(OrderAssertions.uuidsOf(patientOrders))
                .as("order uuids from GET match POST /encounter")
                .isEqualTo(OrderAssertions.uuidsOf(encounter));
    }

    @ParameterizedTest(name = "{0} {1}, {2} -> dateActivated + {3} - 1s")
    @MethodSource("durations")
    public void autoExpireDateIsCalculatedFromDuration(int duration,
                                                       DurationUnit unit,
                                                       OrderFrequency frequency,
                                                       TemporalAmount expectedPeriod) {
        var order = validOutpatientOrder()
                .frequency(frequency)
                .duration(duration)
                .durationUnits(unit)
                .build();

        new SuccessfulCrudRequester<CreateEncounterResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.ENCOUNTER_POST,
                ResponseSpecs.requestReturnsCreated()
        )
                .create(encounterWith(order));

        var orders = getPatientOrders().getResults();
        assertThat(orders)
                .as("precondition: patient has exactly one drug order")
                .hasSize(1);
        var saved = orders.get(0);
        assertThat(saved.getAutoExpireDate())
                .as("autoExpireDate is calculated")
                .isNotNull();

        var dateActivated = OffsetDateTime.parse(saved.getDateActivated(), OPENMRS_DATE);
        softly.assertThat(OffsetDateTime.parse(saved.getAutoExpireDate(), OPENMRS_DATE))
                .as("autoExpireDate = dateActivated (%s) + %s - 1 second", dateActivated, expectedPeriod)
                .isEqualTo(dateActivated.plus(expectedPeriod).minusSeconds(1));
    }

    @ParameterizedTest(name = "{0} -> {2}")
    @MethodSource("invalidDrugOrders")
    public void adminCannotCreateInvalidDrugOrder(String caseName,
                                                  UnaryOperator<DrugOrderBuilder> mutation,
                                                  DrugOrderFieldError error) {
        var order = mutation.apply(validOutpatientOrder()).build();

        new CrudRequester(
                RequestSpecs.adminSpec(),
                Endpoint.ENCOUNTER_POST,
                ResponseSpecs.requestReturnsInvalidSubmission(error)
        )
                .create(encounterWith(order));

        softly.assertThat(getPatientOrders().getResults())
                .as("invalid order is not saved")
                .isEmpty();
    }

    @Test
    @DisplayName("[known issue] admin cannot create second active order for the same drug (server returns 500)")
    public void adminCannotCreateSecondActiveOrderForSameDrug() {
        new SuccessfulCrudRequester<CreateEncounterResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.ENCOUNTER_POST,
                ResponseSpecs.requestReturnsCreated()
        )
                .create(encounterWith(validOutpatientOrder().build()));

        new CrudRequester(
                RequestSpecs.adminSpec(),
                Endpoint.ENCOUNTER_POST,
                ResponseSpecs.requestReturnsBadRequestWithMessage(OrderErrorMessage.MORE_THAN_ONE_ACTIVE_ORDER)
        )
                .create(encounterWith(validOutpatientOrder().build()));

        softly.assertThat(getPatientOrders().getResults())
                .as("only first order is saved")
                .hasSize(1);
    }

    // ======== HELPERS ========
    // Valid outpatient order with simple dosing: baseline for all cases
    private DrugOrderBuilder validOutpatientOrder() {
        return DrugOrder.builder()
                .patient(patientUUID)
                .careSetting(CareSetting.OUTPATIENT)
                .orderer(ordererUUID)
                .drug(Drug.ASPIRIN_325MG)
                .dose(1.0)
                .doseUnits(DosingUnit.TABLET)
                .route(DrugRoute.ORAL)
                .frequency(OrderFrequency.ONCE_DAILY)
                .quantity(5.0)
                .quantityUnits(DosingUnit.TABLET)
                .numRefills(1);
    }

    private CreateEncounterRequest encounterWith(DrugOrder order) {
        return CreateEncounterRequest.builder()
                .patient(patientUUID)
                .encounterType(EncounterType.ORDER)
                .location(Location.OUTPATIENT_CLINIC)
                .orders(List.of(order))
                .build();
    }

    private GetOrderResponse getPatientOrders() {
        return new SuccessfulCrudRequester<GetOrderResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.ORDER_GET,
                ResponseSpecs.requestReturnsOk()
        )
                .get(Map.of("patient", patientUUID, "t", "drugorder", "v", "full"));
    }

    // only for type inference of lambdas inside Arguments.of(...)
    private static UnaryOperator<DrugOrderBuilder> mutate(UnaryOperator<DrugOrderBuilder> mutation) {
        return mutation;
    }
}
