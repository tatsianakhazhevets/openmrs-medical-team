package apiTests.procedures;

import apiParts.assertions.ModelAssertions;
import apiParts.generators.RandomModelGenerator;
import apiParts.assertions.ProcedureAssertions;
import apiParts.models.HasUuid;
import apiParts.models.errors.ProcedureErrorMessage;
import apiParts.models.errors.ProcedureGlobalError;
import apiParts.models.order.DurationUnit;
import apiParts.models.procedure.BodySite;
import apiParts.models.procedure.CreateProcedureRequest;
import apiParts.models.procedure.CreateProcedureRequest.CreateProcedureRequestBuilder;
import apiParts.models.procedure.GetProceduresResponse;
import apiParts.models.procedure.ProcedureConcept;
import apiParts.models.procedure.ProcedureResponse;
import apiParts.models.procedure.ProcedureStatus;
import apiParts.models.procedure.ProcedureType;
import apiParts.skelethon.endpoints.Endpoint;
import apiParts.skelethon.requests.common.CrudRequester;
import apiParts.skelethon.requests.common.SuccessfulCrudRequester;
import apiParts.models.procedure.ProcedureSearchParams;
import apiParts.skelethon.requests.search.SuccessfulSearchRequester;
import apiParts.specs.RequestSpecs;
import apiParts.specs.ResponseSpecs;
import apiParts.testdata.ProcedureTestData;
import apiTests.BaseTest;
import common.annotations.CreatePatient;
import common.storages.SessionStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.UnaryOperator;
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
    // Day in the past, truncated to minutes: server does not store milliseconds
    private static final OffsetDateTime START = ProcedureTestData.PROCEDURE_START;
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
                // emptyOrNonExistent - fabric for test cases. 15 cases in 5 strings using mutation for builder
                emptyOrNonExistent("patient", CreateProcedureRequestBuilder::patient, PATIENT_REQUIRED),
                emptyOrNonExistent("procedureType", CreateProcedureRequestBuilder::procedureType, PROCEDURE_TYPE_REQUIRED),
                emptyOrNonExistent("procedureCoded", CreateProcedureRequestBuilder::procedureCoded, PROCEDURE_REQUIRED),
                emptyOrNonExistent("bodySite", CreateProcedureRequestBuilder::bodySite, BODY_SITE_REQUIRED),
                emptyOrNonExistent("status", CreateProcedureRequestBuilder::status, STATUS_REQUIRED),
                Stream.of(
                        // startDateTime: "" and invalid value fail on conversion, see adminCannotCreateProcedureWithInvalidStartDateTime
                        Arguments.of("startDateTime = null", mutate(b -> b.startDateTime(null)), START_DATE_TIME_REQUIRED),

                        // Dates
                        Arguments.of("endDateTime before startDateTime",
                                mutate(b -> b.endDateTime(format(START.minusMinutes(1)))), END_DATE_TIME_BEFORE_START_DATE_TIME),
                        Arguments.of("[known issue] completed procedure with startDateTime in the future",
                                mutate(b -> b.startDateTime(format(OffsetDateTime.now(MOSCOW)
                                        .plusDays(RandomModelGenerator.randomInt(MIN_DAYS_IN_FUTURE, MAX_DAYS_IN_FUTURE))))),
                        START_DATE_TIME_IN_FUTURE),
                        Arguments.of("startDateTime and estimatedStartDate for new procedure",
                                mutate(b -> b.estimatedStartDate(START.format(DateTimeFormatter.ofPattern("yyyy-MM")))), START_DATE_TIME_AND_ESTIMATED_DATE_MUTUALLY_EXCLUSIVE),

                        // Duration
                        Arguments.of("duration without durationUnit", mutate(b -> b.duration(randomDuration())), DURATION_UNIT_REQUIRED),

                        // Procedure
                        Arguments.of("procedureCoded and procedureNonCoded",
                                mutate(b -> b.procedureNonCoded(NON_CODED_PROCEDURE)), PROCEDURE_CODED_AND_NON_CODED_MUTUALLY_EXCLUSIVE)
                )
        ).flatMap(cases -> cases);
    }

    @ParameterizedTest(name = "#{index}: {0} / {1} / {2}, {3}, {4}, duration {5} {6}")
    @MethodSource("pairwiseProcedures")
    public void adminCanCreateProcedure(ProcedureConcept procedureCoded,
                                        String procedureNonCoded,
                                        ProcedureType procedureType,
                                        BodySite bodySite,
                                        ProcedureStatus status,
                                        Integer duration,
                                        DurationUnit durationUnit) {
        var request = CreateProcedureRequest.builder()
                .patient(patientUUID)
                .procedureCoded(uuidOf(procedureCoded))
                .procedureNonCoded(procedureNonCoded)
                .procedureType(procedureType.getUuid())
                .bodySite(bodySite.getUuid())
                .startDateTime(format(START))
                .status(status.getUuid())
                .duration(duration)
                .durationUnit(uuidOf(durationUnit))
                .notes(RandomModelGenerator.randomSentence())
                .build();

        var procedure = new SuccessfulCrudRequester<ProcedureResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.PROCEDURE_POST,
                ResponseSpecs.requestReturnsCreated()
        )
                .create(request);

        var patientProcedures = getPatientProcedures();

        ModelAssertions.assertListMatchesExpected(softly,
                patientProcedures.getResults(),
                ProcedureAssertions.expectedProceduresOf(request),
                ProcedureAssertions::procedureCodedUuidOf,
                "procedures saved for patient");
        softly.assertThat(ProcedureAssertions.uuidsOf(patientProcedures))
                .as("procedure uuids from GET match POST /procedure")
                .isEqualTo(ProcedureAssertions.uuidsOf(procedure));
    }

    @ParameterizedTest(name = "{0} -> {2}")
    @MethodSource("invalidProcedures")
    public void adminCannotCreateInvalidProcedure(String caseName,
                                                  UnaryOperator<CreateProcedureRequestBuilder> mutation,
                                                  ProcedureGlobalError error) {
        var request = mutation.apply(validProcedure()).build();

        new CrudRequester(
                RequestSpecs.adminSpec(),
                Endpoint.PROCEDURE_POST,
                ResponseSpecs.requestReturnsInvalidSubmission(error)
        )
                .create(request);

        softly.assertThat(getPatientProcedures().getResults())
                .as("invalid procedure is not saved")
                .isEmpty();
    }

    // Not ISO-8601 value fails on conversion before validation: 400 without globalErrors
    static Stream<String> invalidStartDateTimes() {
        return Stream.of(
                "",
                RandomModelGenerator.randomWord(),
                START.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")));
    }

    @ParameterizedTest(name = "startDateTime = \"{0}\"")
    @MethodSource("invalidStartDateTimes")
    public void adminCannotCreateProcedureWithInvalidStartDateTime(String startDateTime) {
        var request = validProcedure()
                .startDateTime(startDateTime)
                .build();

        new CrudRequester(
                RequestSpecs.adminSpec(),
                Endpoint.PROCEDURE_POST,
                ResponseSpecs.requestReturnsBadRequestWithMessage(ProcedureErrorMessage.DATE_CONVERSION_ERROR)
        )
                .create(request);

        softly.assertThat(getPatientProcedures().getResults())
                .as("procedure with invalid startDateTime is not saved")
                .isEmpty();
    }

    // Server returns 400 "Privileges required: Get Patients" (not 401): it fails on converting patient uuid
    @Test
    public void unauthorizedUserCannotCreateProcedure() {
        var request = validProcedure().build();

        new CrudRequester(
                RequestSpecs.unAuthSpec(),
                Endpoint.PROCEDURE_POST,
                ResponseSpecs.requestReturnsBadRequestWithMessage(ProcedureErrorMessage.PRIVILEGES_REQUIRED_GET_PATIENTS)
        )
                .create(request);

        softly.assertThat(getPatientProcedures().getResults())
                .as("procedure of unauthorized user is not saved")
                .isEmpty();
    }

    // ======== HELPERS ========
    // Valid procedure with required fields only: baseline for negative cases
    private CreateProcedureRequestBuilder validProcedure() {
        return CreateProcedureRequest.builder()
                .patient(patientUUID)
                .procedureCoded(LAPAROSCOPIC_CHOLECYSTECTOMY.getUuid())
                .procedureType(EMERGENCY.getUuid())
                .bodySite(ABDOMEN.getUuid())
                .startDateTime(format(START))
                .status(COMPLETED.getUuid());
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
                Arguments.of(field + " = null", mutate(b -> setter.apply(b, null)), error),
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

    private static String uuidOf(HasUuid value) {
        return value == null ? null : value.getUuid();
    }

    // only for type inference of lambdas inside Arguments.of(...)
    private static UnaryOperator<CreateProcedureRequestBuilder> mutate(UnaryOperator<CreateProcedureRequestBuilder> mutation) {
        return mutation;
    }

    // ==== Search-based variant of the "procedure was persisted" check. Originals untouched. ====
    @Test
    public void createdProcedureIsReturnedBySearchViaSearchRequester() {
        CreateProcedureRequest request = validProcedure().build();

        ProcedureResponse created = new SuccessfulCrudRequester<ProcedureResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.PROCEDURE_POST,
                ResponseSpecs.requestReturnsCreated())
                .create(request);

        ProcedureResponse found = new SuccessfulSearchRequester<ProcedureResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.PROCEDURES_GET,
                ResponseSpecs.requestReturnsOk())
                .search(ProcedureSearchParams.builder()
                        .patient(patientUUID)
                        .representation("full")
                        .build())
                .requireOne(procedure -> procedure.getUuid().equals(created.getUuid()),
                        "created procedure " + created.getUuid());

        ModelAssertions.assertMatchesExpected(softly,
                found,
                ProcedureAssertions.expectedProcedureOf(request),
                "procedure found by search");
    }

}
