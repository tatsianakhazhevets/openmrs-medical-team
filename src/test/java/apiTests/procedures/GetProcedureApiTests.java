package apiTests.procedures;

import apiParts.assertions.ModelAssertions;
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
import apiTests.BaseTest;
import common.annotations.CreatePatient;
import common.annotations.CreateProcedure;
import common.storages.SessionStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static apiParts.models.order.DurationUnit.HOURS;
import static apiParts.models.procedure.BodySite.ABDOMEN;
import static apiParts.models.procedure.ProcedureConcept.LAPAROSCOPIC_CHOLECYSTECTOMY;
import static apiParts.models.procedure.ProcedureStatus.COMPLETED;
import static apiParts.models.procedure.ProcedureType.EMERGENCY;
import static apiParts.utils.DateTimeUtils.OPENMRS_REQUEST_DATE_TIME;

@CreatePatient
public class GetProcedureApiTests extends BaseTest {
    private static final ZoneOffset MOSCOW = ZoneOffset.ofHours(3);
    // yesterday, truncated to minutes: server does not store milliseconds
    private static final OffsetDateTime START = OffsetDateTime.now(MOSCOW).minusDays(1).truncatedTo(ChronoUnit.MINUTES);

    private String patientUUID;

    @BeforeEach
    void setUp() {
        patientUUID = SessionStorage.getPatient().getUuid();
    }

    @Test
    public void adminCanGetProcedureByUuid() {
        var request = CreateProcedureRequest.builder()
                .patient(patientUUID)
                .procedureCoded(LAPAROSCOPIC_CHOLECYSTECTOMY.getUuid())
                .procedureType(EMERGENCY.getUuid())
                .bodySite(ABDOMEN.getUuid())
                .startDateTime(format(START))
                .endDateTime(format(START.plusHours(3)))
                .status(COMPLETED.getUuid())
                .duration(3)
                .durationUnit(HOURS.getUuid())
                .notes("done smth")
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

    // each procedure has its own procedureCoded - it is the sort key for list comparison
    @ParameterizedTest(name = "patient with {0} procedure(s)")
    @ValueSource(ints = {0, 1, 3})
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
