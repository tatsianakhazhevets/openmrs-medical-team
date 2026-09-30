package apiTests.procedures;

import apiParts.assertions.ModelAssertions;
import apiParts.generators.RandomModelGenerator;
import apiParts.models.HasUuid;
import apiParts.models.errors.ProcedureErrorMessage;
import apiParts.models.errors.ProcedureGlobalError;
import apiParts.models.order.DurationUnit;
import apiParts.models.procedure.BodySite;
import apiParts.models.procedure.CreateProcedureRequest;
import apiParts.models.procedure.ProcedureConcept;
import apiParts.models.procedure.ProcedureResponse;
import apiParts.models.procedure.ProcedureStatus;
import apiParts.models.procedure.ProcedureType;
import apiParts.skelethon.endpoints.Endpoint;
import apiParts.skelethon.requests.crud.CrudRequester;
import apiParts.skelethon.requests.crud.SuccessfulCrudRequester;
import apiParts.models.search.SearchResult;
import apiParts.models.procedure.ProcedureSearchParams;
import apiParts.skelethon.requests.search.SuccessfulSearchRequester;
import apiParts.specs.RequestSpecs;
import apiParts.specs.ResponseSpecs;
import apiParts.utils.DateTimeUtils;
import apiParts.utils.Uuids;
import apiTests.BaseTest;
import common.annotations.CreatePatient;
import common.storages.SessionStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static apiParts.models.errors.ProcedureGlobalError.*;
import static apiParts.models.order.DurationUnit.*;
import static apiParts.models.procedure.BodySite.*;
import static apiParts.models.procedure.ProcedureConcept.*;
import static apiParts.models.procedure.ProcedureStatus.*;
import static apiParts.models.procedure.ProcedureType.*;
import static apiParts.utils.DateTimeUtils.MOSCOW;
import static apiParts.utils.DateTimeUtils.OPENMRS_REQUEST_DATE_TIME;

@CreatePatient
public class CreateProcedureApiTests extends BaseTest {
    private static final String NON_CODED_PROCEDURE = RandomModelGenerator.randomSentence();

    // Any duration is handled by the same server logic
    private static final int MIN_DURATION = 1;
    private static final int MAX_DURATION = 10;
    private static final int MIN_DAYS_IN_FUTURE = 1;
    private static final int MAX_DAYS_IN_FUTURE = 30;

    private String patientUUID;

    // new patient for each test and each parameter - only procedures of this test are returned for the patient
    @BeforeEach
    void setUp() {
        patientUUID = SessionStorage.getPatient().getUuid();
    }

    // Pairwise: each pair of values of any two parameters is covered exactly once.
    // Server does not restrict combinations (and concept classes), so all rows are valid.
    // procedure: coded Procedure class (CIEL uuid) / coded Radiology-Imaging class / coded Procedure class (local uuid) / non-coded text
    static Stream<Arguments> pairwiseProcedures() {
        return Stream.of(
                // procedureCoded | procedureNonCoded | procedureType | bodySite | status | duration | durationUnit
                Arguments.of(LAPAROSCOPIC_CHOLECYSTECTOMY, null, SURGICAL, ABDOMEN, COMPLETED, null, null),
                Arguments.of(LAPAROSCOPIC_CHOLECYSTECTOMY, null, IMAGING, CHEST, NOT_DONE, randomDuration(), DAYS),
                Arguments.of(LAPAROSCOPIC_CHOLECYSTECTOMY, null, VACCINATION, EYE, PREPARATION, randomDuration(), MINUTES),
                Arguments.of(LAPAROSCOPIC_CHOLECYSTECTOMY, null, OTHER, SKIN, IN_PROGRESS, randomDuration(), HOURS),
                Arguments.of(X_RAY_CHEST, null, SURGICAL, CHEST, IN_PROGRESS, randomDuration(), MINUTES),
                Arguments.of(X_RAY_CHEST, null, IMAGING, ABDOMEN, PREPARATION, randomDuration(), HOURS),
                Arguments.of(X_RAY_CHEST, null, VACCINATION, SKIN, NOT_DONE, null, null),
                Arguments.of(X_RAY_CHEST, null, OTHER, EYE, COMPLETED, randomDuration(), DAYS),
                Arguments.of(INFLUENZA_VACCINATION, null, SURGICAL, EYE, NOT_DONE, randomDuration(), HOURS),
                Arguments.of(INFLUENZA_VACCINATION, null, IMAGING, SKIN, COMPLETED, randomDuration(), MINUTES),
                Arguments.of(INFLUENZA_VACCINATION, null, VACCINATION, ABDOMEN, IN_PROGRESS, randomDuration(), DAYS),
                Arguments.of(INFLUENZA_VACCINATION, null, OTHER, CHEST, PREPARATION, null, null),
                Arguments.of(null, NON_CODED_PROCEDURE, SURGICAL, SKIN, PREPARATION, randomDuration(), DAYS),
                Arguments.of(null, NON_CODED_PROCEDURE, IMAGING, EYE, IN_PROGRESS, null, null),
                Arguments.of(null, NON_CODED_PROCEDURE, VACCINATION, CHEST, COMPLETED, randomDuration(), HOURS),
                Arguments.of(null, NON_CODED_PROCEDURE, OTHER, ABDOMEN, NOT_DONE, randomDuration(), MINUTES)
        );
    }

    // Each case = valid procedure + one change -> expected ProcedureValidator error (400, globalErrors)
    static Stream<Arguments> invalidProcedures() {
        return Stream.of(
                // Required references: null (not sent), "" and non-existent uuid (server converts it to null)
                // emptyOrNonExistent - fabric for test cases. 15 cases in 5 strings using setter of the field
                emptyOrNonExistent("patient", CreateProcedureRequest::setPatient, PATIENT_REQUIRED),
                emptyOrNonExistent("procedureType", CreateProcedureRequest::setProcedureType, PROCEDURE_TYPE_REQUIRED),
                emptyOrNonExistent("procedureCoded", CreateProcedureRequest::setProcedureCoded, PROCEDURE_REQUIRED),
                emptyOrNonExistent("bodySite", CreateProcedureRequest::setBodySite, BODY_SITE_REQUIRED),
                emptyOrNonExistent("status", CreateProcedureRequest::setStatus, STATUS_REQUIRED),
                Stream.of(
                        // startDateTime: "" and invalid value fail on conversion, see adminCannotCreateProcedureWithInvalidStartDateTime
                        // endDateTime is removed too: with endDateTime server fails with 500 (NPE in ProcedureValidator:48)
                        Arguments.of("startDateTime = null", mutate(r -> {
                            r.setStartDateTime(null);
                            r.setEndDateTime(null);
                        }), START_DATE_TIME_REQUIRED),

                        // Dates
                        // dates are relative to startDateTime of the generated request (see startOf)
                        Arguments.of("endDateTime before startDateTime",
                                mutate(r -> r.setEndDateTime(format(startOf(r).minusMinutes(1)))), END_DATE_TIME_BEFORE_START_DATE_TIME),
                        // status is fixed and endDateTime removed: otherwise 400 could come from another rule
                        Arguments.of("[known issue] completed procedure with startDateTime in the future",
                                mutate(r -> {
                                    r.setStatus(COMPLETED.getUuid());
                                    r.setEndDateTime(null);
                                    r.setStartDateTime(format(OffsetDateTime.now(MOSCOW)
                                            .plusDays(RandomModelGenerator.randomInt(MIN_DAYS_IN_FUTURE, MAX_DAYS_IN_FUTURE))));
                                }),
                                START_DATE_TIME_IN_FUTURE),
                        Arguments.of("startDateTime and estimatedStartDate for new procedure",
                                mutate(r -> r.setEstimatedStartDate(startOf(r).format(DateTimeFormatter.ofPattern("yyyy-MM")))), START_DATE_TIME_AND_ESTIMATED_DATE_MUTUALLY_EXCLUSIVE),

                        // Duration: generated request has durationUnit, so it is removed
                        Arguments.of("duration without durationUnit", mutate(r -> r.setDurationUnit(null)), DURATION_UNIT_REQUIRED),

                        // Procedure
                        Arguments.of("procedureCoded and procedureNonCoded",
                                mutate(r -> r.setProcedureNonCoded(NON_CODED_PROCEDURE)), PROCEDURE_CODED_AND_NON_CODED_MUTUALLY_EXCLUSIVE)
                )
        ).flatMap(cases -> cases);
    }

    @Test
    public void adminCanCreateProcedure() {
        var request = RandomModelGenerator.generate(CreateProcedureRequest.class);

        var procedure = new SuccessfulCrudRequester<ProcedureResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.PROCEDURE_POST,
                ResponseSpecs.requestReturnsCreated()
        )
                .create(request);
        ModelAssertions.assertThatModels(request, procedure)
                .as("POST /procedure response")
                .match();

        var patientProcedures = getPatientProcedures();

        ModelAssertions.assertThatModels(List.of(request), patientProcedures.results())
                .as("procedures saved for patient")
                .match();
        softly.assertThat(Uuids.of(patientProcedures.results()))
                .as("procedure uuids from GET match POST /procedure")
                .isEqualTo(Set.of(procedure.getUuid()));

    }

    @ParameterizedTest(name = "#{index}: {0} / {1} / {2}, {3}, {4}, duration {5} {6}")
    @MethodSource("pairwiseProcedures")
    public void adminCanCreateProcedureWithDifferentTypes(ProcedureConcept procedureCoded,
                                        String procedureNonCoded,
                                        ProcedureType procedureType,
                                        BodySite bodySite,
                                        ProcedureStatus status,
                                        Integer duration,
                                        DurationUnit durationUnit) {
        // random procedure, pairwise fields are overridden (null = field is not sent)
        var request = RandomModelGenerator.generate(CreateProcedureRequest.class);
        request.setProcedureCoded(uuidOf(procedureCoded));
        request.setProcedureNonCoded(procedureNonCoded);
        request.setProcedureType(procedureType.getUuid());
        request.setBodySite(bodySite.getUuid());
        request.setStatus(status.getUuid());
        request.setDuration(duration);
        request.setDurationUnit(uuidOf(durationUnit));

        var procedure = new SuccessfulCrudRequester<ProcedureResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.PROCEDURE_POST,
                ResponseSpecs.requestReturnsCreated()
        )
                .create(request);
        ModelAssertions.assertThatModels(request, procedure)
                .as("POST /procedure response")
                .match();

        var patientProcedures = getPatientProcedures();

        ModelAssertions.assertThatModels(List.of(request), patientProcedures.results())
                .as("procedures saved for patient")
                .match();
        softly.assertThat(Uuids.of(patientProcedures.results()))
                .as("procedure uuids from GET match POST /procedure")
                .isEqualTo(Set.of(procedure.getUuid()));
    }

    @ParameterizedTest(name = "{0} -> {2}")
    @MethodSource("invalidProcedures")
    public void adminCannotCreateInvalidProcedure(String caseName,
                                                  Consumer<CreateProcedureRequest> mutation,
                                                  ProcedureGlobalError error) {
        var request = RandomModelGenerator.generate(CreateProcedureRequest.class);
        mutation.accept(request);
        var before = getPatientProcedures().results();

        new CrudRequester(
                RequestSpecs.adminSpec(),
                Endpoint.PROCEDURE_POST,
                ResponseSpecs.requestReturnsInvalidSubmission(error)
        )
                .create(request);

        ModelAssertions.assertUnchanged(before, getPatientProcedures().results(),
                "patient procedures after invalid POST /procedure");
    }

    // Not ISO-8601 value fails on conversion before validation: 400 without globalErrors
    static Stream<String> invalidStartDateTimes() {
        return Stream.of(
                "",
                RandomModelGenerator.randomWord(),
                DateTimeUtils.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")));
    }

    @ParameterizedTest(name = "startDateTime = \"{0}\"")
    @MethodSource("invalidStartDateTimes")
    public void adminCannotCreateProcedureWithInvalidStartDateTime(String startDateTime) {
        var request = RandomModelGenerator.generate(CreateProcedureRequest.class);
        request.setStartDateTime(startDateTime);
        var before = getPatientProcedures().results();

        new CrudRequester(
                RequestSpecs.adminSpec(),
                Endpoint.PROCEDURE_POST,
                ResponseSpecs.requestReturnsBadRequestWithMessage(ProcedureErrorMessage.DATE_CONVERSION_ERROR)
        )
                .create(request);

        ModelAssertions.assertUnchanged(before, getPatientProcedures().results(),
                "patient procedures after POST /procedure with invalid startDateTime");
    }

    // Server returns 400 "Privileges required: Get Patients" (not 401): it fails on converting patient uuid
    @Test
    public void unauthorizedUserCannotCreateProcedure() {
        var request = RandomModelGenerator.generate(CreateProcedureRequest.class);
        var before = getPatientProcedures().results();

        new CrudRequester(
                RequestSpecs.unAuthSpec(),
                Endpoint.PROCEDURE_POST,
                ResponseSpecs.requestReturnsBadRequestWithMessage(ProcedureErrorMessage.PRIVILEGES_REQUIRED_GET_PATIENTS)
        )
                .create(request);

        ModelAssertions.assertUnchanged(before, getPatientProcedures().results(),
                "patient procedures after unauthorized POST /procedure");
    }

    // ======== HELPERS ========
    private SearchResult<ProcedureResponse> getPatientProcedures() {
        return new SuccessfulSearchRequester<ProcedureResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.PROCEDURES_GET,
                ResponseSpecs.requestReturnsOk()
        )
                .search(ProcedureSearchParams.builder()
                        .patient(patientUUID)
                        .representation("full")
                        .build());
    }

    private static Stream<Arguments> emptyOrNonExistent(String field,
                                                        BiConsumer<CreateProcedureRequest, String> setter,
                                                        ProcedureGlobalError error) {
        return Stream.of(
                Arguments.of(field + " = null", mutate(r -> setter.accept(r, null)), error),
                Arguments.of(field + " = \"\"", mutate(r -> setter.accept(r, "")), error),
                Arguments.of(field + " = non-existent uuid", mutate(r -> setter.accept(r, UUID.randomUUID().toString())), error)
        );
    }

    private static int randomDuration() {
        return RandomModelGenerator.randomInt(MIN_DURATION, MAX_DURATION);
    }

    private static String format(OffsetDateTime dateTime) {
        return dateTime.format(OPENMRS_REQUEST_DATE_TIME);
    }

    private static OffsetDateTime startOf(CreateProcedureRequest request) {
        return OffsetDateTime.parse(request.getStartDateTime(), OPENMRS_REQUEST_DATE_TIME);
    }

    private static String uuidOf(HasUuid value) {
        return value == null ? null : value.getUuid();
    }

    // only for type inference of lambdas inside Arguments.of(...)
    private static Consumer<CreateProcedureRequest> mutate(Consumer<CreateProcedureRequest> mutation) {
        return mutation;
    }
}
