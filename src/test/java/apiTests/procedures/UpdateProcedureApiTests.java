package apiTests.procedures;

import apiParts.steps.ProcedureSteps;
import apiParts.assertions.ModelAssertions;
import apiParts.generators.RandomModelGenerator;
import apiParts.models.errors.ProcedureGlobalError;
import apiParts.models.procedure.CreateProcedureRequest;
import apiParts.models.procedure.ProcedureResponse;
import apiParts.skelethon.endpoints.Endpoint;
import apiParts.skelethon.requests.crud.CrudRequester;
import apiParts.skelethon.requests.crud.SuccessfulCrudRequester;
import apiParts.specs.RequestSpecs;
import apiParts.specs.ResponseSpecs;
import apiParts.utils.Uuids;
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
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

import static apiParts.utils.DateTimeUtils.toRequestString;
import static apiParts.models.errors.ProcedureGlobalError.*;
import static apiParts.models.order.DurationUnit.DAYS;
import static apiParts.models.procedure.BodySite.CHEST;
import static apiParts.models.procedure.ProcedureConcept.X_RAY_CHEST;
import static apiParts.models.procedure.ProcedureStatus.IN_PROGRESS;
import static apiParts.models.procedure.ProcedureType.SURGICAL;
import static org.assertj.core.api.Assertions.assertThat;

// POST /procedure/{uuid} is a partial update: request contains only changed fields, other fields stay as created
@CreatePatient
@CreateProcedure
public class UpdateProcedureApiTests extends BaseTest {
    private String patientUUID;
    private CreateProcedureRequest createRequest;
    private ProcedureResponse procedure;

    // Precondition: patient with one valid procedure (@CreatePatient, @CreateProcedure)
    @BeforeEach
    void setUp() {
        patientUUID = SessionStorage.getPatient().getUuid();
        createRequest = SessionStorage.getProcedureRequest();
        procedure = SessionStorage.getProcedure();
    }

    // Each case = one change, set by setters. The same mutation fills partial update request (new, empty)
    // and expected procedure (request of procedure from @CreateProcedure + change).
    // Procedure from @CreateProcedure is random: mutation gets its request (created) to build dates relative to it
    // The same mutation is applied twice (update request and expected procedure), so generated
    // values are taken here and captured by the lambda - a call inside it would give two results
    static Stream<Arguments> validUpdates() {
        int daysEarlier = ProcedureSteps.randomDuration();
        int hoursLater = ProcedureSteps.randomDuration();
        int duration = ProcedureSteps.randomDuration();
        String notes = RandomModelGenerator.randomSentence();
        String otherNotes = RandomModelGenerator.randomSentence();

        return Stream.of(
                Arguments.of("procedureCoded", mutate((r, created) -> r.setProcedureCoded(X_RAY_CHEST.getUuid()))),
                Arguments.of("procedureType", mutate((r, created) -> r.setProcedureType(SURGICAL.getUuid()))),
                Arguments.of("bodySite", mutate((r, created) -> r.setBodySite(CHEST.getUuid()))),
                Arguments.of("status", mutate((r, created) -> r.setStatus(IN_PROGRESS.getUuid()))),
                Arguments.of("startDateTime", mutate((r, created) -> r.setStartDateTime(toRequestString(ProcedureSteps.startDateTimeOf(created).minusDays(daysEarlier))))),
                Arguments.of("endDateTime", mutate((r, created) -> r.setEndDateTime(toRequestString(ProcedureSteps.startDateTimeOf(created).plusHours(hoursLater))))),
                Arguments.of("duration with durationUnit", mutate((r, created) -> {
                    r.setDuration(duration);
                    r.setDurationUnit(DAYS.getUuid());
                })),
                Arguments.of("notes", mutate((r, created) -> r.setNotes(notes))),
                Arguments.of("several fields", mutate((r, created) -> {
                    r.setBodySite(CHEST.getUuid());
                    r.setStatus(IN_PROGRESS.getUuid());
                    r.setNotes(otherNotes);
                }))
        );
    }

    // Each case = one invalid change -> expected ProcedureValidator error (400, globalErrors), procedure is not changed
    static Stream<Arguments> invalidUpdates() {
        return Stream.of(
                // Required references: "" and non-existent uuid (null is not sent = no change)
                emptyOrNonExistent(CreateProcedureRequest.Fields.patient, CreateProcedureRequest::setPatient, PATIENT_REQUIRED),
                emptyOrNonExistent(CreateProcedureRequest.Fields.procedureType, CreateProcedureRequest::setProcedureType, PROCEDURE_TYPE_REQUIRED),
                emptyOrNonExistent(CreateProcedureRequest.Fields.procedureCoded, CreateProcedureRequest::setProcedureCoded, PROCEDURE_REQUIRED),
                emptyOrNonExistent(CreateProcedureRequest.Fields.bodySite, CreateProcedureRequest::setBodySite, BODY_SITE_REQUIRED),
                emptyOrNonExistent(CreateProcedureRequest.Fields.status, CreateProcedureRequest::setStatus, STATUS_REQUIRED),
                Stream.of(
                        Arguments.of("endDateTime before startDateTime",
                                mutate((r, created) -> r.setEndDateTime(toRequestString(ProcedureSteps.startDateTimeOf(created).minusMinutes(1)))), END_DATE_TIME_BEFORE_START_DATE_TIME),
                        // created procedure already has durationUnit (partial update keeps it), so it is cleared by ""
                        Arguments.of("duration without durationUnit",
                                mutate((r, created) -> {
                                    r.setDuration(ProcedureSteps.randomDuration());
                                    r.setDurationUnit("");
                                }), DURATION_UNIT_REQUIRED),
                        Arguments.of("procedureNonCoded to procedure with procedureCoded",
                                mutate((r, created) -> r.setProcedureNonCoded(RandomModelGenerator.randomSentence())), PROCEDURE_CODED_AND_NON_CODED_MUTUALLY_EXCLUSIVE)
                )
        ).flatMap(cases -> cases);
    }

    @ParameterizedTest(name = "update {0}")
    @MethodSource("validUpdates")
    public void adminCanUpdateProcedure(String caseName, Mutation mutation) {
        var updateRequest = new CreateProcedureRequest();
        mutation.accept(updateRequest, createRequest);
        // procedure from @CreateProcedure + change: request from SessionStorage is new for each test, so it is changed in place
        var expected = createRequest;
        mutation.accept(expected, createRequest);

        var updated = new SuccessfulCrudRequester<ProcedureResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.PROCEDURE_UPDATE,
                ResponseSpecs.requestReturnsOk()
        )
                .update(procedure.getUuid(), updateRequest);
        ModelAssertions.assertThatModels(expected, updated)
                .as("POST /procedure/{uuid} response")
                .match();

        ModelAssertions.assertThatModels(expected, ProcedureSteps.getProcedure(procedure.getUuid()))
                .as("updated procedure")
                .match();
        softly.assertThat(Uuids.of(ProcedureSteps.getPatientProcedures(patientUUID).results()))
                .as("update does not create new procedure")
                .isEqualTo(Set.of(procedure.getUuid()));
    }

    // startDateTime and estimatedStartDate are mutually exclusive only for new procedures.
    // For existing one server calculates startDateTime = start of estimated period
    // in server timezone (ProcedureUtil uses ZoneId.systemDefault(), UTC in docker)
    @ParameterizedTest(name = "estimatedStartDate in format {0}")
    @ValueSource(strings = {"yyyy", "yyyy-MM", "yyyy-MM-dd"})
    public void adminCanSetEstimatedStartDateForExistingProcedure(String pattern) {
        var start = ProcedureSteps.startDateTimeOf(createRequest);
        var estimatedStartDate = start.format(DateTimeFormatter.ofPattern(pattern));
        var updateRequest = new CreateProcedureRequest();
        updateRequest.setEstimatedStartDate(estimatedStartDate);

        var updated = new SuccessfulCrudRequester<ProcedureResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.PROCEDURE_UPDATE,
                ResponseSpecs.requestReturnsOk()
        )
                .update(procedure.getUuid(), updateRequest);

        LocalDate periodStart = switch (pattern) {
            case "yyyy" -> start.toLocalDate().withDayOfYear(1);
            case "yyyy-MM" -> start.toLocalDate().withDayOfMonth(1);
            default -> start.toLocalDate();
        };
        var expected = createRequest;
        expected.setEstimatedStartDate(estimatedStartDate);
        expected.setStartDateTime(toRequestString(periodStart.atStartOfDay().atOffset(ZoneOffset.UTC)));

        ModelAssertions.assertThatModels(expected, updated)
                .as("POST /procedure/{uuid} response")
                .match();
        ModelAssertions.assertThatModels(expected, ProcedureSteps.getProcedure(procedure.getUuid()))
                .as("procedure with estimatedStartDate")
                .match();
    }

    @ParameterizedTest(name = "{0} -> {2}")
    @MethodSource("invalidUpdates")
    public void adminCannotUpdateProcedureWithInvalidData(String caseName,
                                                          Mutation mutation,
                                                          ProcedureGlobalError error) {
        var updateRequest = new CreateProcedureRequest();
        mutation.accept(updateRequest, createRequest);
        var before = ProcedureSteps.getProcedure(procedure.getUuid());

        new CrudRequester(
                RequestSpecs.adminSpec(),
                Endpoint.PROCEDURE_UPDATE,
                ResponseSpecs.requestReturnsInvalidSubmission(error)
        )
                .update(procedure.getUuid(), updateRequest);

        ModelAssertions.assertUnchanged(before, ProcedureSteps.getProcedure(procedure.getUuid()), "procedure after invalid update");
    }

    @Test
    public void adminCannotUpdateNonExistentProcedure() {
        var updateRequest = new CreateProcedureRequest();
        updateRequest.setNotes(RandomModelGenerator.randomSentence());
        var before = ProcedureSteps.getProcedure(procedure.getUuid());

        new CrudRequester(
                RequestSpecs.adminSpec(),
                Endpoint.PROCEDURE_UPDATE,
                ResponseSpecs.requestReturnsNotFound()
        )
                .update(UUID.randomUUID().toString(), updateRequest);

        ModelAssertions.assertUnchanged(before, ProcedureSteps.getProcedure(procedure.getUuid()), "existing procedure after update of non-existent one");
    }

    @Test
    public void unauthorizedUserCannotUpdateProcedure() {
        var updateRequest = new CreateProcedureRequest();
        updateRequest.setNotes(RandomModelGenerator.randomSentence());
        var before = ProcedureSteps.getProcedure(procedure.getUuid());

        new CrudRequester(
                RequestSpecs.unAuthSpec(),
                Endpoint.PROCEDURE_UPDATE,
                ResponseSpecs.requestReturnsUnauthorized()
        )
                .update(procedure.getUuid(), updateRequest);

        ModelAssertions.assertUnchanged(before, ProcedureSteps.getProcedure(procedure.getUuid()), "procedure after unauthorized update");
    }

    // ======== HELPERS (test case factories for @MethodSource) ========
    private static Stream<Arguments> emptyOrNonExistent(String field,
                                                        BiConsumer<CreateProcedureRequest, String> setter,
                                                        ProcedureGlobalError error) {
        return Stream.of(
                Arguments.of(field + " = \"\"", mutate((r, created) -> setter.accept(r, "")), error),
                Arguments.of(field + " = non-existent uuid", mutate((r, created) -> setter.accept(r, UUID.randomUUID().toString())), error)
        );
    }

    // (request to change by setters, request of procedure from @CreateProcedure)
    interface Mutation extends BiConsumer<CreateProcedureRequest, CreateProcedureRequest> {
    }

    // only for type inference of lambdas inside Arguments.of(...)
    private static Mutation mutate(Mutation mutation) {
        return mutation;
    }
}
