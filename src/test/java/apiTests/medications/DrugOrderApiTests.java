package apiTests.medications;

import apiParts.assertions.ModelAssertions;
import apiParts.generators.RandomModelGenerator;
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
import apiParts.models.order.DrugOrderResponse;
import apiParts.models.order.OrderSearchParams;
import apiParts.skelethon.requests.search.SuccessfulSearchRequester;
import apiParts.specs.RequestSpecs;
import apiParts.specs.ResponseSpecs;
import apiParts.steps.AdminSteps;
import apiParts.testdata.OrderTestData;
import apiTests.BaseTest;
import common.annotations.CreatePatient;
import common.storages.SessionStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.Period;
import java.time.temporal.TemporalAmount;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import static apiParts.models.errors.DrugOrderFieldError.*;
import static apiParts.utils.DateTimeUtils.OPENMRS_RESPONSE_DATE_TIME;
import static org.assertj.core.api.Assertions.assertThat;

@CreatePatient
public class DrugOrderApiTests extends BaseTest {
    // Any duration inside these bounds is handled by the same server logic
    private static final int MIN_DURATION = 1;
    private static final int MAX_DURATION = 10;
    private static final int MINUTES_PER_DAY = 24 * 60;

    // Dose below one unit: server must keep the fractional part as sent
    private static final double MIN_FRACTIONAL_DOSE = 0.1;
    private static final double MAX_FRACTIONAL_DOSE = 0.9;
    private static final int DOSE_SCALE = 1;

    // Lower bounds for invalid values: anything below zero must be rejected
    private static final double MIN_INVALID_DOSE = -100;
    private static final int MIN_INVALID_NUM_REFILLS = -100;

    private String patientUUID;
    private String ordererUUID;

    // new patient for each test and each parameter - no "duplicate active order" conflicts
    @BeforeEach
    void setUp() {
        patientUUID = SessionStorage.getPatient().getUuid();
        ordererUUID = AdminSteps.getCurrentProviderUuid();
    }

    // Each case = valid outpatient order + one change, server must save it as sent
    static Stream<Arguments> validDrugOrders() {
        return Stream.of(
                Arguments.of("valid outpatient order", mutate(b -> b)),
                Arguments.of("fractional dose", mutate(b -> b.dose(
                        RandomModelGenerator.randomDouble(MIN_FRACTIONAL_DOSE, MAX_FRACTIONAL_DOSE, DOSE_SCALE)))),
                Arguments.of("quantityUnits from dispensing set", mutate(b -> b.quantityUnits(DosingUnit.BOTTLE))),
                Arguments.of("inpatient without quantity, quantityUnits, numRefills", mutate(b -> b
                        .careSetting(CareSetting.INPATIENT)
                        .quantity(null).quantityUnits(null).numRefills(null))),
                Arguments.of("duration with durationUnits", mutate(b -> b
                        .duration(randomDuration())
                        .durationUnits(RandomModelGenerator.oneOf(DurationUnit.class)))),
                Arguments.of("durationUnits without duration", mutate(b -> b
                        .durationUnits(RandomModelGenerator.oneOf(DurationUnit.class)))),
                Arguments.of("without concept - server takes it from drug", mutate(b -> b.concept(null))),
                Arguments.of("free text dosing with dosingInstructions only", mutate(b -> b
                        .dosingType(DrugOrder.FREE_TEXT_DOSING)
                        .dosingInstructions(RandomModelGenerator.randomSentence())
                        .dose(null).doseUnits(null).route(null).frequency(null))),
                Arguments.of("asNeeded without asNeededCondition", mutate(b -> b.asNeeded(true))),

                // Server does not validate combinations of fields - current behavior is fixed here
                Arguments.of("tablet with intravenous route", mutate(b -> b.route(DrugRoute.INTRAVENOUS))),
                Arguments.of("cream drug dosed in tablets orally", mutate(b -> b.drug(Drug.ACYCLOVIR_CREAM_3)))
        );
    }

    // autoExpireDate = dateActivated + duration - 1 second (server ends order "a moment before").
    // Every duration unit with the default frequency + OCCURRENCES with the second one:
    // it is the only unit whose period depends on frequency
    static Stream<Arguments> durations() {
        return Stream.concat(
                Arrays.stream(DurationUnit.values())
                        .map(unit -> durationCase(unit, OrderFrequency.ONCE_DAILY)),
                Stream.of(durationCase(DurationUnit.OCCURRENCES, OrderFrequency.TWICE_DAILY)));
    }

    // Each case = valid outpatient order + one change -> expected validation error
    static Stream<Arguments> invalidDrugOrders() {
        return Stream.of(
                // Simple dosing: required fields and dose > 0
                Arguments.of("dose = 0", mutate(b -> b.dose(0.0)), DOSE_ZERO_OR_LESS),
                Arguments.of("negative dose", mutate(b -> b.dose(
                        RandomModelGenerator.randomNegativeDouble(MIN_INVALID_DOSE, DOSE_SCALE))), DOSE_ZERO_OR_LESS),
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
                Arguments.of("duration without durationUnits", mutate(b -> b.duration(randomDuration())),
                        DURATION_UNITS_REQUIRED_WITH_DURATION),

                // Drug and concept
                Arguments.of("concept does not match drug",
                        mutate(b -> b.concept(Drug.ACETAMINOPHEN_325MG.getConceptUuid())), CONCEPT_NOT_MATCHING_DRUG),

                // KNOWN ISSUES: server accepts these (201), test is expected to fail until fixed
                Arguments.of("[known issue] quantity = 0", mutate(b -> b.quantity(0.0)), QUANTITY_ZERO_OR_LESS),
                Arguments.of("[known issue] negative numRefills", mutate(b -> b.numRefills(
                        RandomModelGenerator.randomNegativeInt(MIN_INVALID_NUM_REFILLS))), NUM_REFILLS_NEGATIVE)
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

        var dateActivated = OffsetDateTime.parse(saved.getDateActivated(), OPENMRS_RESPONSE_DATE_TIME);
        softly.assertThat(OffsetDateTime.parse(saved.getAutoExpireDate(), OPENMRS_RESPONSE_DATE_TIME))
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
    // Valid outpatient order with simple dosing: baseline for all cases, same fixture as @CreateOrder(DRUG)
    private DrugOrderBuilder validOutpatientOrder() {
        return OrderTestData.validOutpatientDrugOrder(patientUUID, ordererUUID);
    }

    private static int randomDuration() {
        return RandomModelGenerator.randomInt(MIN_DURATION, MAX_DURATION);
    }

    private static Arguments durationCase(DurationUnit unit, OrderFrequency frequency) {
        int duration = randomDuration();
        return Arguments.of(duration, unit, frequency, periodOf(duration, unit, frequency));
    }

    // Period the server adds to dateActivated for the given duration
    private static TemporalAmount periodOf(int duration, DurationUnit unit, OrderFrequency frequency) {
        return switch (unit) {
            case SECONDS -> Duration.ofSeconds(duration);
            case MINUTES -> Duration.ofMinutes(duration);
            case HOURS -> Duration.ofHours(duration);
            case DAYS -> Period.ofDays(duration);
            case WEEKS -> Period.ofWeeks(duration);
            case MONTHS -> Period.ofMonths(duration);
            case YEARS -> Period.ofYears(duration);
            // duration = number of doses, period = doses / frequency per day: 3 doses twice a day = 36 hours
            case OCCURRENCES -> Duration.ofMinutes((long) duration * MINUTES_PER_DAY / frequency.getDosesPerDay());
        };
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

    // ==== Search-based variant of getPatientOrders(). Original helper untouched. ====
    // Same call, but "t" and "v" are fields instead of string keys in a Map,
    // so a typo is a compile error rather than a differently filtered request.
    @Test
    public void createdDrugOrderIsFoundBySearchViaSearchRequester() {
        DrugOrder order = validOutpatientOrder().build();
        CreateEncounterRequest encounterRequest = encounterWith(order);

        CreateEncounterResponse encounter = new SuccessfulCrudRequester<CreateEncounterResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.ENCOUNTER_POST,
                ResponseSpecs.requestReturnsCreated())
                .create(encounterRequest);

        softly.assertThat(encounter.getOrders()).as("created drug order").hasSize(1);
        String orderUUID = encounter.getOrders().get(0).getUuid();

        DrugOrderResponse savedOrder = new SuccessfulSearchRequester<DrugOrderResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.ORDER_GET,
                ResponseSpecs.requestReturnsOk())
                .search(OrderSearchParams.builder()
                        .patient(patientUUID)
                        .type("drugorder")
                        .representation("full")
                        .build())
                .requireOne(found -> found.getUuid().equals(orderUUID),
                        "created order " + orderUUID);

        softly.assertThat(savedOrder.getUuid()).isEqualTo(orderUUID);
    }

}
