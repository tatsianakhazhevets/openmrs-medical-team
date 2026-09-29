package apiTests.medications;

import apiParts.assertions.ModelAssertions;
import apiParts.generators.RandomModelGenerator;
import apiParts.models.encounter.CreateEncounterRequest;
import apiParts.models.encounter.CreateEncounterResponse;
import apiParts.models.errors.DrugOrderFieldError;
import apiParts.models.errors.OrderErrorMessage;
import apiParts.models.order.CareSetting;
import apiParts.models.order.DosingUnit;
import apiParts.models.order.Drug;
import apiParts.models.order.DrugOrder;
import apiParts.models.order.DrugRoute;
import apiParts.models.order.DurationUnit;
import apiParts.models.order.OrderFrequency;
import apiParts.skelethon.endpoints.Endpoint;
import apiParts.skelethon.requests.crud.CrudRequester;
import apiParts.skelethon.requests.crud.SuccessfulCrudRequester;
import apiParts.models.search.SearchResult;
import apiParts.models.order.DrugOrderResponse;
import apiParts.models.order.OrderSearchParams;
import apiParts.skelethon.requests.search.SuccessfulSearchRequester;
import apiParts.specs.RequestSpecs;
import apiParts.specs.ResponseSpecs;
import apiParts.utils.Uuids;
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
import java.util.function.Consumer;
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

    // new patient for each test and each parameter - no "duplicate active order" conflicts
    @BeforeEach
    void setUp() {
        patientUUID = SessionStorage.getPatient().getUuid();
    }

    // Each case = valid random outpatient order (rules in DrugOrder) + one change by setters,
    // server must save it as sent
    static Stream<Arguments> validDrugOrders() {
        return Stream.of(
                Arguments.of("valid outpatient order", mutate(o -> {
                })),
                Arguments.of("fractional dose", mutate(o -> o.setDose(
                        RandomModelGenerator.randomDouble(MIN_FRACTIONAL_DOSE, MAX_FRACTIONAL_DOSE, DOSE_SCALE)))),
                Arguments.of("quantityUnits from dispensing set", mutate(o -> o.setQuantityUnits(DosingUnit.BOTTLE))),
                Arguments.of("inpatient without quantity, quantityUnits, numRefills", mutate(o -> {
                    o.setCareSetting(CareSetting.INPATIENT);
                    o.setQuantity(null);
                    o.setQuantityUnits(null);
                    o.setNumRefills(null);
                })),
                Arguments.of("duration with durationUnits", mutate(o -> {
                    o.setDuration(randomDuration());
                    o.setDurationUnits(RandomModelGenerator.oneOf(DurationUnit.class));
                })),
                Arguments.of("durationUnits without duration", mutate(o ->
                        o.setDurationUnits(RandomModelGenerator.oneOf(DurationUnit.class)))),
                Arguments.of("without concept - server takes it from drug", mutate(o -> o.setConcept(null))),
                Arguments.of("free text dosing with dosingInstructions only", mutate(o -> {
                    o.setDosingType(DrugOrder.FREE_TEXT_DOSING);
                    o.setDosingInstructions(RandomModelGenerator.randomSentence());
                    o.setDose(null);
                    o.setDoseUnits(null);
                    o.setRoute(null);
                    o.setFrequency(null);
                })),
                Arguments.of("asNeeded without asNeededCondition", mutate(o -> o.setAsNeeded(true))),

                // Server does not validate combinations of fields - current behavior is fixed here
                Arguments.of("tablet with intravenous route", mutate(o -> o.setRoute(DrugRoute.INTRAVENOUS))),
                Arguments.of("cream drug dosed in tablets orally", mutate(o -> o.setDrug(Drug.ACYCLOVIR_CREAM_3)))
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

    // Each case = valid random outpatient order + one change by setters -> expected validation error
    static Stream<Arguments> invalidDrugOrders() {
        return Stream.of(
                // Simple dosing: required fields and dose > 0
                Arguments.of("dose = 0", mutate(o -> o.setDose(0.0)), DOSE_ZERO_OR_LESS),
                Arguments.of("negative dose", mutate(o -> o.setDose(
                        RandomModelGenerator.randomNegativeDouble(MIN_INVALID_DOSE, DOSE_SCALE))), DOSE_ZERO_OR_LESS),
                Arguments.of("simple dosing without dose", mutate(o -> o.setDose(null)), DOSE_IS_NULL_FOR_SIMPLE_DOSING),
                Arguments.of("simple dosing without route", mutate(o -> o.setRoute(null)), ROUTE_IS_NULL_FOR_SIMPLE_DOSING),
                Arguments.of("doseUnits from dispensing set only", mutate(o -> o.setDoseUnits(DosingUnit.BOTTLE)),
                        DOSE_UNITS_NOT_AMONG_ALLOWED),

                // Free text dosing
                Arguments.of("free text dosing without dosingInstructions", mutate(o -> {
                            o.setDosingType(DrugOrder.FREE_TEXT_DOSING);
                            o.setDose(null);
                            o.setDoseUnits(null);
                            o.setRoute(null);
                            o.setFrequency(null);
                        }),
                        DOSING_INSTRUCTIONS_IS_NULL_FOR_FREE_TEXT_DOSING),

                // Outpatient (drugOrder.requireOutpatientQuantity = true)
                Arguments.of("outpatient without quantity", mutate(o -> o.setQuantity(null)), QUANTITY_IS_NULL_FOR_OUTPATIENT),
                Arguments.of("outpatient without quantityUnits", mutate(o -> o.setQuantityUnits(null)),
                        QUANTITY_UNITS_REQUIRED_WITH_QUANTITY),
                Arguments.of("outpatient without numRefills", mutate(o -> o.setNumRefills(null)), NUM_REFILLS_IS_NULL_FOR_OUTPATIENT),

                // Duration
                Arguments.of("duration without durationUnits", mutate(o -> o.setDuration(randomDuration())),
                        DURATION_UNITS_REQUIRED_WITH_DURATION),

                // Drug and concept: drug is random, so concept is taken from any other drug
                Arguments.of("concept does not match drug",
                        mutate(o -> o.setConcept(RandomModelGenerator.oneOfExcept(Drug.class, o.getDrug()).getConceptUuid())),
                        CONCEPT_NOT_MATCHING_DRUG),

                // KNOWN ISSUES: server accepts these (201), test is expected to fail until fixed
                Arguments.of("[known issue] quantity = 0", mutate(o -> o.setQuantity(0.0)), QUANTITY_ZERO_OR_LESS),
                Arguments.of("[known issue] negative numRefills", mutate(o -> o.setNumRefills(
                        RandomModelGenerator.randomNegativeInt(MIN_INVALID_NUM_REFILLS))), NUM_REFILLS_NEGATIVE)
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("validDrugOrders")
    public void adminCanCreateDrugOrder(String caseName, Consumer<DrugOrder> mutation) {
        var order = RandomModelGenerator.generate(DrugOrder.class);
        mutation.accept(order);
        var request = encounterWith(order);

        var encounter = new SuccessfulCrudRequester<CreateEncounterResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.ENCOUNTER_POST,
                ResponseSpecs.requestReturnsCreated()
        )
                .create(request);
        ModelAssertions.assertThatModels(softly, request, encounter)
                .as("POST /encounter response")
                .match();
        softly.assertThat(encounter.getOrders())
                .as("orders in POST /encounter response")
                .hasSize(request.getOrders().size());

        var patientOrders = getPatientOrders();

        ModelAssertions.assertThatModels(softly, request.getOrders(), patientOrders.results())
                .as("drug orders saved for patient")
                .match();
        // concept may be omitted in request ("without concept" case) - server takes it from drug
        softly.assertThat(patientOrders.results().stream().map(o -> o.getConcept().getUuid()).toList())
                .as("concept of drug order is the concept of its drug")
                .containsExactlyElementsOf(request.getOrders().stream()
                        .map(o -> ((DrugOrder) o).getDrug().getConceptUuid())
                        .toList());
        softly.assertThat(Uuids.of(patientOrders.results()))
                .as("order uuids from GET match POST /encounter")
                .isEqualTo(Uuids.of(encounter.getOrders()));
    }

    @ParameterizedTest(name = "{0} {1}, {2} -> dateActivated + {3} - 1s")
    @MethodSource("durations")
    public void autoExpireDateIsCalculatedFromDuration(int duration,
                                                       DurationUnit unit,
                                                       OrderFrequency frequency,
                                                       TemporalAmount expectedPeriod) {
        var order = RandomModelGenerator.generate(DrugOrder.class);
        order.setFrequency(frequency);
        order.setDuration(duration);
        order.setDurationUnits(unit);

        var request = encounterWith(order);
        var encounter = new SuccessfulCrudRequester<CreateEncounterResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.ENCOUNTER_POST,
                ResponseSpecs.requestReturnsCreated()
        )
                .create(request);
        ModelAssertions.assertThatModels(softly, request, encounter)
                .as("POST /encounter response")
                .match();

        var orders = getPatientOrders().results();
        assertThat(orders)
                .as("precondition: patient has exactly one drug order")
                .hasSize(1);
        ModelAssertions.assertThatModels(softly, List.of(order), orders)
                .as("drug order saved for patient")
                .match();
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
                                                  Consumer<DrugOrder> mutation,
                                                  DrugOrderFieldError error) {
        var order = RandomModelGenerator.generate(DrugOrder.class);
        mutation.accept(order);
        var before = getPatientOrders().results();

        new CrudRequester(
                RequestSpecs.adminSpec(),
                Endpoint.ENCOUNTER_POST,
                ResponseSpecs.requestReturnsInvalidSubmission(error)
        )
                .create(encounterWith(order));

        ModelAssertions.assertUnchanged(softly, before, getPatientOrders().results(),
                "patient drug orders after invalid POST /encounter");
    }

    @Test
    @DisplayName("[known issue] admin cannot create second active order for the same drug (server returns 500)")
    public void adminCannotCreateSecondActiveOrderForSameDrug() {
        var firstOrder = RandomModelGenerator.generate(DrugOrder.class);
        new SuccessfulCrudRequester<CreateEncounterResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.ENCOUNTER_POST,
                ResponseSpecs.requestReturnsCreated()
        )
                .create(encounterWith(firstOrder));
        var before = getPatientOrders().results();

        // another random order for the same drug
        var secondOrder = RandomModelGenerator.generate(DrugOrder.class);
        secondOrder.setDrug(firstOrder.getDrug());

        new CrudRequester(
                RequestSpecs.adminSpec(),
                Endpoint.ENCOUNTER_POST,
                ResponseSpecs.requestReturnsBadRequestWithMessage(OrderErrorMessage.MORE_THAN_ONE_ACTIVE_ORDER)
        )
                .create(encounterWith(secondOrder));

        ModelAssertions.assertUnchanged(softly, before, getPatientOrders().results(),
                "patient drug orders after second active order for the same drug");
    }

    // ======== HELPERS ========
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

    // Random order encounter (rules in CreateEncounterRequest) with the given order
    private static CreateEncounterRequest encounterWith(DrugOrder order) {
        var request = RandomModelGenerator.generate(CreateEncounterRequest.class);
        request.setOrders(List.of(order));
        return request;
    }

    private SearchResult<DrugOrderResponse> getPatientOrders() {
        return new SuccessfulSearchRequester<DrugOrderResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.ORDER_GET,
                ResponseSpecs.requestReturnsOk()
        )
                .search(OrderSearchParams.builder()
                        .patient(patientUUID)
                        .type("drugorder")
                        .representation("full")
                        .build());
    }

    // only for type inference of lambdas inside Arguments.of(...)
    private static Consumer<DrugOrder> mutate(Consumer<DrugOrder> mutation) {
        return mutation;
    }
}
