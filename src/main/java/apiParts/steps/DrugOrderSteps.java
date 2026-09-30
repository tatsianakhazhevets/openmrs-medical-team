package apiParts.steps;

import apiParts.models.GetParams;
import apiParts.generators.RandomModelGenerator;
import apiParts.models.encounter.CreateEncounterRequest;
import apiParts.models.order.DrugOrder;
import apiParts.models.order.DrugOrderResponse;
import apiParts.models.order.DurationUnit;
import apiParts.models.order.OrderFrequency;
import apiParts.models.order.OrderSearchParams;
import apiParts.models.search.SearchResult;
import apiParts.skelethon.endpoints.Endpoint;
import apiParts.skelethon.requests.search.SuccessfulSearchRequester;
import apiParts.specs.RequestSpecs;
import apiParts.specs.ResponseSpecs;

import java.time.Duration;
import java.time.Period;
import java.time.temporal.TemporalAmount;
import java.util.List;

/**
 * Steps for drug order tests: order encounter, patient drug orders and values for test cases.
 */
public class DrugOrderSteps {
    // Any duration inside these bounds is handled by the same server logic
    private static final int MIN_DURATION = 1;
    private static final int MAX_DURATION = 10;
    private static final int MINUTES_PER_DAY = 24 * 60;

    private DrugOrderSteps() {
    }

    // Random order encounter (rules in CreateEncounterRequest) with the given order
    public static CreateEncounterRequest encounterWith(DrugOrder order) {
        var request = RandomModelGenerator.generate(CreateEncounterRequest.class);
        request.setOrders(List.of(order));
        return request;
    }

    // GET /order?patient={uuid}&t=drugorder&v=full
    public static SearchResult<DrugOrderResponse> getPatientDrugOrders(String patientUUID) {
        return new SuccessfulSearchRequester<DrugOrderResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.ORDER_GET,
                ResponseSpecs.requestReturnsOk()
        )
                .search(OrderSearchParams.builder()
                        .patient(patientUUID)
                        .type("drugorder")
                        .representation(GetParams.FULL)
                        .build());
    }

    public static int randomDuration() {
        return RandomModelGenerator.randomInt(MIN_DURATION, MAX_DURATION);
    }

    // Period the server adds to dateActivated for the given duration (autoExpireDate = dateActivated + period - 1s)
    public static TemporalAmount durationPeriod(int duration, DurationUnit unit, OrderFrequency frequency) {
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
}
