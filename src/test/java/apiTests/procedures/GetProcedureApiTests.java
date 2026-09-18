package apiTests.procedures;

import apiParts.assertions.ModelAssertions;
import apiParts.generators.RandomModelGenerator;
import apiParts.assertions.ProcedureAssertions;
import apiParts.models.errors.ProcedureErrorMessage;
import apiParts.models.procedure.CreateProcedureRequest;
import apiParts.models.procedure.GetProceduresResponse;
import apiParts.models.procedure.ProcedureConcept;
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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static apiParts.models.order.DurationUnit.HOURS;
import static apiParts.models.procedure.BodySite.ABDOMEN;
import static apiParts.models.procedure.ProcedureConcept.LAPAROSCOPIC_CHOLECYSTECTOMY;
import static apiParts.models.procedure.ProcedureStatus.COMPLETED;
import static apiParts.models.procedure.ProcedureType.EMERGENCY;
import static apiParts.utils.DateTimeUtils.OPENMRS_REQUEST_DATE_TIME;

@CreatePatient
public class GetProcedureApiTests extends BaseTest {
    // Day in the past, truncated to minutes: server does not store milliseconds
    private static final OffsetDateTime START = ProcedureTestData.PROCEDURE_START;

    // Any duration is handled by the same server logic
    private static final int MIN_DURATION = 1;
    private static final int MAX_DURATION = 10;

    private String patientUUID;

    @BeforeEach
    void setUp() {
        patientUUID = SessionStorage.getPatient().getUuid();
    }

    @Test
    public void adminCanGetProcedureByUuid() {
        // procedure lasted exactly its duration: endDateTime and duration are built from one value
        int durationInHours = RandomModelGenerator.randomInt(MIN_DURATION, MAX_DURATION);
        var request = CreateProcedureRequest.builder()
                .patient(patientUUID)
                .procedureCoded(LAPAROSCOPIC_CHOLECYSTECTOMY.getUuid())
                .procedureType(EMERGENCY.getUuid())
                .bodySite(ABDOMEN.getUuid())
                .startDateTime(format(START))
                .endDateTime(format(START.plusHours(durationInHours)))
                .status(COMPLETED.getUuid())
                .duration(durationInHours)
                .durationUnit(HOURS.getUuid())
                .notes(RandomModelGenerator.randomSentence())
                .build();

        var procedure = createProcedure(request);

        var savedProcedure = new SuccessfulCrudRequester<ProcedureResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.PROCEDURE_GET,
                ResponseSpecs.requestReturnsOk()
        )
                .get(procedure.getUuid());

        softly.assertThat(savedProcedure.getUuid())
                .as("procedure uuid from GET matches POST /procedure")
                .isEqualTo(procedure.getUuid());
        ModelAssertions.assertMatchesExpected(softly,
                savedProcedure,
                ProcedureAssertions.expectedProcedureOf(request),
                "procedure returned by GET /procedure/{uuid}");
    }

    // each procedure has its own procedureCoded - it is the sort key for list comparison,
    // so the patient cannot have more procedures than there are concepts
    static Stream<Integer> proceduresCounts() {
        return Stream.of(0, 1, ProcedureConcept.values().length);
    }

    @ParameterizedTest(name = "patient with {0} procedure(s)")
    @MethodSource("proceduresCounts")
    public void adminCanGetAllProceduresOfPatient(int proceduresCount) {
        List<CreateProcedureRequest> requests = new ArrayList<>();
        List<ProcedureResponse> procedures = new ArrayList<>();

        for (int i = 0; i < proceduresCount; i++) {
            var request = validProcedure()
                    .procedureCoded(ProcedureConcept.values()[i].getUuid())
                    .build();
            requests.add(request);
            procedures.add(createProcedure(request));
        }

        var patientProcedures = getPatientProcedures();

        ModelAssertions.assertListMatchesExpected(softly,
                patientProcedures.getResults(),
                ProcedureAssertions.expectedProceduresOf(requests),
                ProcedureAssertions::procedureCodedUuidOf,
                "procedures returned for patient");
        softly.assertThat(ProcedureAssertions.uuidsOf(patientProcedures))
                .as("procedure uuids from GET match POST /procedure")
                .isEqualTo(ProcedureAssertions.uuidsOf(procedures));
    }

    @Test
    public void adminCannotGetNonExistentProcedure() {
        new CrudRequester(
                RequestSpecs.adminSpec(),
                Endpoint.PROCEDURE_GET,
                ResponseSpecs.requestReturnsNotFound()
        )
                .get(UUID.randomUUID().toString());
    }

    // search by patient is the only supported search
    @Test
    public void adminCannotGetProceduresWithoutPatient() {
        new CrudRequester(
                RequestSpecs.adminSpec(),
                Endpoint.PROCEDURES_GET,
                ResponseSpecs.requestReturnsBadRequestWithMessage(ProcedureErrorMessage.OPERATION_NOT_SUPPORTED)
        )
                .get(Map.of("v", "full"));
    }

    @Test
    @DisplayName("[known issue] admin cannot get procedures of non-existent patient (server returns 500)")
    public void adminCannotGetProceduresOfNonExistentPatient() {
        new CrudRequester(
                RequestSpecs.adminSpec(),
                Endpoint.PROCEDURES_GET,
                ResponseSpecs.requestReturnsBadRequestWithMessage(ProcedureErrorMessage.PATIENT_NOT_FOUND)
        )
                .get(Map.of("patient", UUID.randomUUID().toString(), "v", "full"));
    }

    @Test
    @CreateProcedure
    public void unauthorizedUserCannotGetProcedureByUuid() {
        var procedure = SessionStorage.getProcedure();

        new CrudRequester(
                RequestSpecs.unAuthSpec(),
                Endpoint.PROCEDURE_GET,
                ResponseSpecs.requestReturnsUnauthorized()
        )
                .get(procedure.getUuid());
    }

    @Test
    @CreateProcedure
    public void unauthorizedUserCannotGetProceduresOfPatient() {
        new CrudRequester(
                RequestSpecs.unAuthSpec(),
                Endpoint.PROCEDURES_GET,
                ResponseSpecs.requestReturnsUnauthorized()
        )
                .get(Map.of("patient", patientUUID, "v", "full"));
    }

    // ======== HELPERS ========
    // Valid procedure with required fields only
    private CreateProcedureRequest.CreateProcedureRequestBuilder validProcedure() {
        return CreateProcedureRequest.builder()
                .patient(patientUUID)
                .procedureCoded(LAPAROSCOPIC_CHOLECYSTECTOMY.getUuid())
                .procedureType(EMERGENCY.getUuid())
                .bodySite(ABDOMEN.getUuid())
                .startDateTime(format(START))
                .status(COMPLETED.getUuid());
    }

    private ProcedureResponse createProcedure(CreateProcedureRequest request) {
        return new SuccessfulCrudRequester<ProcedureResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.PROCEDURE_POST,
                ResponseSpecs.requestReturnsCreated()
        )
                .create(request);
    }

    private GetProceduresResponse getPatientProcedures() {
        return new SuccessfulCrudRequester<GetProceduresResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.PROCEDURES_GET,
                ResponseSpecs.requestReturnsOk()
        )
                .get(Map.of("patient", patientUUID, "v", "full"));
    }

    private static String format(OffsetDateTime dateTime) {
        return dateTime.format(OPENMRS_REQUEST_DATE_TIME);
    }
}
