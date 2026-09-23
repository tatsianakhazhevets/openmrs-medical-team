package apiTests.procedures;

import apiParts.assertions.ModelAssertions;
import apiParts.generators.RandomModelGenerator;
import apiParts.assertions.ProcedureAssertions;
import apiParts.models.errors.ProcedureGlobalError;
import apiParts.models.procedure.CreateProcedureRequest;
import apiParts.models.procedure.CreateProcedureRequest.CreateProcedureRequestBuilder;
import apiParts.models.procedure.GetProceduresResponse;
import apiParts.models.procedure.ProcedureResponse;
import apiParts.skelethon.endpoints.Endpoint;
import apiParts.skelethon.requests.common.CrudRequester;
import apiParts.skelethon.requests.common.SuccessfulCrudRequester;
import apiParts.specs.RequestSpecs;
import apiParts.specs.ResponseSpecs;
import apiParts.testdata.ProcedureTestData;
import apiTests.BaseTest;
import common.annotations.CreatePatient;
import common.annotations.CreateProcedure;
import common.storages.SessionStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import static apiParts.models.errors.ProcedureGlobalError.*;
import static apiParts.models.order.DurationUnit.DAYS;
import static apiParts.models.procedure.BodySite.CHEST;
import static apiParts.models.procedure.ProcedureConcept.X_RAY_CHEST;
import static apiParts.models.procedure.ProcedureStatus.IN_PROGRESS;
import static apiParts.models.procedure.ProcedureType.SURGICAL;
import static apiParts.utils.DateTimeUtils.OPENMRS_REQUEST_DATE_TIME;
import static org.assertj.core.api.Assertions.assertThat;

// POST /procedure/{uuid} is a partial update: request contains only changed fields, other fields stay as created
@CreatePatient
@CreateProcedure
public class UpdateProcedureApiTests extends BaseTest {
    // startDateTime of procedure from @CreateProcedure - dates in cases are relative to it
    private static final OffsetDateTime START = ProcedureTestData.PROCEDURE_START;
    private static final String NON_CODED_PROCEDURE = RandomModelGenerator.randomSentence();

    // Any duration is handled by the same server logic
    private static final int MIN_DURATION = 1;
    private static final int MAX_DURATION = 10;

    private String patientUUID;
    private CreateProcedureRequest createRequest;
    private ProcedureResponse procedure;

    // Precondition: patient with one valid procedure (@CreatePatient, @CreateProcedure)
    @BeforeEach
    void setUp() {
        patientUUID = SessionStorage.getPatient().getUuid();
        createRequest = ProcedureTestData.procedureRequest(patientUUID);
        procedure = SessionStorage.getProcedure();
    }

    // Each case = one change. The same mutation builds partial update request (empty builder)
    // and expected procedure (request of procedure from @CreateProcedure + change)
    // The same mutation is applied twice (update request and expected procedure), so generated
    // values are taken here and captured by the lambda - a call inside it would give two results
    static Stream<Arguments> validUpdates() {
        int daysEarlier = randomDuration();
        int hoursLater = randomDuration();
        int duration = randomDuration();
        String notes = RandomModelGenerator.randomSentence();
        String otherNotes = RandomModelGenerator.randomSentence();

        return Stream.of(
                Arguments.of("procedureCoded", mutate(b -> b.procedureCoded(X_RAY_CHEST.getUuid()))),
                Arguments.of("procedureType", mutate(b -> b.procedureType(SURGICAL.getUuid()))),
                Arguments.of("bodySite", mutate(b -> b.bodySite(CHEST.getUuid()))),
                Arguments.of("status", mutate(b -> b.status(IN_PROGRESS.getUuid()))),
                Arguments.of("startDateTime", mutate(b -> b.startDateTime(format(START.minusDays(daysEarlier))))),
                Arguments.of("endDateTime", mutate(b -> b.endDateTime(format(START.plusHours(hoursLater))))),
                Arguments.of("duration with durationUnit", mutate(b -> b.duration(duration).durationUnit(DAYS.getUuid()))),
                Arguments.of("notes", mutate(b -> b.notes(notes))),
                Arguments.of("several fields", mutate(b -> b
                        .bodySite(CHEST.getUuid())
                        .status(IN_PROGRESS.getUuid())
                        .notes(otherNotes)))
        );
    }

    // Each case = one invalid change -> expected ProcedureValidator error (400, globalErrors), procedure is not changed
    static Stream<Arguments> invalidUpdates() {
        return Stream.of(
                // Required references: "" and non-existent uuid (null is not sent = no change)
                emptyOrNonExistent("patient", CreateProcedureRequestBuilder::patient, PATIENT_REQUIRED),
                emptyOrNonExistent("procedureType", CreateProcedureRequestBuilder::procedureType, PROCEDURE_TYPE_REQUIRED),
                emptyOrNonExistent("procedureCoded", CreateProcedureRequestBuilder::procedureCoded, PROCEDURE_REQUIRED),
                emptyOrNonExistent("bodySite", CreateProcedureRequestBuilder::bodySite, BODY_SITE_REQUIRED),
                emptyOrNonExistent("status", CreateProcedureRequestBuilder::status, STATUS_REQUIRED),
                Stream.of(
                        Arguments.of("endDateTime before startDateTime",
                                mutate(b -> b.endDateTime(format(START.minusMinutes(1)))), END_DATE_TIME_BEFORE_START_DATE_TIME),
                        Arguments.of("duration without durationUnit", mutate(b -> b.duration(randomDuration())), DURATION_UNIT_REQUIRED),
                        Arguments.of("procedureNonCoded to procedure with procedureCoded",
                                mutate(b -> b.procedureNonCoded(NON_CODED_PROCEDURE)), PROCEDURE_CODED_AND_NON_CODED_MUTUALLY_EXCLUSIVE)
                )
        ).flatMap(cases -> cases);
    }

    @ParameterizedTest(name = "update {0}")
    @MethodSource("validUpdates")
    public void adminCanUpdateProcedure(String caseName, UnaryOperator<CreateProcedureRequestBuilder> mutation) {
        var updateRequest = mutation.apply(CreateProcedureRequest.builder()).build();

        new SuccessfulCrudRequester<ProcedureResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.PROCEDURE_UPDATE,
                ResponseSpecs.requestReturnsOk()
        )
                .update(procedure.getUuid(), updateRequest);

        ModelAssertions.assertMatchesExpected(softly,
                getProcedure(),
                ProcedureAssertions.expectedProcedureOf(mutation.apply(createRequest.toBuilder()).build()),
                "updated procedure");
        softly.assertThat(ProcedureAssertions.uuidsOf(getPatientProcedures()))
                .as("update does not create new procedure")
                .isEqualTo(Set.of(procedure.getUuid()));
    }

    // startDateTime and estimatedStartDate are mutually exclusive only for new procedures.
    // For existing one server calculates startDateTime = start of estimated period
    // in server timezone (ProcedureUtil uses ZoneId.systemDefault(), UTC in docker)
    @ParameterizedTest(name = "estimatedStartDate in format {0}")
    @ValueSource(strings = {"yyyy", "yyyy-MM", "yyyy-MM-dd"})
    public void adminCanSetEstimatedStartDateForExistingProcedure(String pattern) {
        var estimatedStartDate = START.format(DateTimeFormatter.ofPattern(pattern));
        var updateRequest = CreateProcedureRequest.builder()
                .estimatedStartDate(estimatedStartDate)
                .build();

        new SuccessfulCrudRequester<ProcedureResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.PROCEDURE_UPDATE,
                ResponseSpecs.requestReturnsOk()
        )
                .update(procedure.getUuid(), updateRequest);

        LocalDate periodStart = switch (pattern) {
            case "yyyy" -> START.toLocalDate().withDayOfYear(1);
            case "yyyy-MM" -> START.toLocalDate().withDayOfMonth(1);
            default -> START.toLocalDate();
        };
        var expected = createRequest.toBuilder()
                .estimatedStartDate(estimatedStartDate)
                .startDateTime(format(periodStart.atStartOfDay().atOffset(ZoneOffset.UTC)))
                .build();

        ModelAssertions.assertMatchesExpected(softly,
                getProcedure(),
                ProcedureAssertions.expectedProcedureOf(expected),
                "procedure with estimatedStartDate");
    }

    @ParameterizedTest(name = "{0} -> {2}")
    @MethodSource("invalidUpdates")
    public void adminCannotUpdateProcedureWithInvalidData(String caseName,
                                                          UnaryOperator<CreateProcedureRequestBuilder> mutation,
                                                          ProcedureGlobalError error) {
        var updateRequest = mutation.apply(CreateProcedureRequest.builder()).build();

        new CrudRequester(
                RequestSpecs.adminSpec(),
                Endpoint.PROCEDURE_UPDATE,
                ResponseSpecs.requestReturnsInvalidSubmission(error)
        )
                .update(procedure.getUuid(), updateRequest);

        ModelAssertions.assertMatchesExpected(softly,
                getProcedure(),
                ProcedureAssertions.expectedProcedureOf(createRequest),
                "procedure is not changed");
    }

    @Test
    public void adminCannotUpdateNonExistentProcedure() {
        var updateRequest = CreateProcedureRequest.builder()
                .notes(RandomModelGenerator.randomSentence())
                .build();

        new CrudRequester(
                RequestSpecs.adminSpec(),
                Endpoint.PROCEDURE_UPDATE,
                ResponseSpecs.requestReturnsNotFound()
        )
                .update(UUID.randomUUID().toString(), updateRequest);

        ModelAssertions.assertMatchesExpected(softly,
                getProcedure(),
                ProcedureAssertions.expectedProcedureOf(createRequest),
                "existing procedure is not changed");
    }

    @Test
    public void unauthorizedUserCannotUpdateProcedure() {
        var updateRequest = CreateProcedureRequest.builder()
                .notes(RandomModelGenerator.randomSentence())
                .build();

        new CrudRequester(
                RequestSpecs.unAuthSpec(),
                Endpoint.PROCEDURE_UPDATE,
                ResponseSpecs.requestReturnsUnauthorized()
        )
                .update(procedure.getUuid(), updateRequest);

        ModelAssertions.assertMatchesExpected(softly,
                getProcedure(),
                ProcedureAssertions.expectedProcedureOf(createRequest),
                "procedure is not changed");
    }

    // ======== HELPERS ========
    private ProcedureResponse getProcedure() {
        var saved = new SuccessfulCrudRequester<ProcedureResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.PROCEDURE_GET,
                ResponseSpecs.requestReturnsOk()
        )
                .get(procedure.getUuid());
        assertThat(saved.getUuid())
                .as("precondition: GET /procedure/{uuid} returns procedure from setUp")
                .isEqualTo(procedure.getUuid());
        return saved;
    }

    private GetProceduresResponse getPatientProcedures() {
        return new SuccessfulCrudRequester<GetProceduresResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.PROCEDURES_GET,
                ResponseSpecs.requestReturnsOk()
        )
                .get(Map.of("patient", patientUUID, "v", "full"));
    }

    private static Stream<Arguments> emptyOrNonExistent(String field,
                                                        BiFunction<CreateProcedureRequestBuilder, String, CreateProcedureRequestBuilder> setter,
                                                        ProcedureGlobalError error) {
        return Stream.of(
                Arguments.of(field + " = \"\"", mutate(b -> setter.apply(b, "")), error),
                Arguments.of(field + " = non-existent uuid", mutate(b -> setter.apply(b, UUID.randomUUID().toString())), error)
        );
    }

    private static int randomDuration() {
        return RandomModelGenerator.randomInt(MIN_DURATION, MAX_DURATION);
    }

    private static String format(OffsetDateTime dateTime) {
        return dateTime.format(OPENMRS_REQUEST_DATE_TIME);
    }

    // only for type inference of lambdas inside Arguments.of(...)
    private static UnaryOperator<CreateProcedureRequestBuilder> mutate(UnaryOperator<CreateProcedureRequestBuilder> mutation) {
        return mutation;
    }
}
