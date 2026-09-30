package apiParts.steps;

import apiParts.models.GetParams;
import apiParts.generators.RandomModelGenerator;
import apiParts.models.procedure.CreateProcedureRequest;
import apiParts.models.procedure.ProcedureResponse;
import apiParts.models.procedure.ProcedureSearchParams;
import apiParts.models.search.SearchResult;
import apiParts.skelethon.endpoints.Endpoint;
import apiParts.skelethon.requests.crud.SuccessfulCrudRequester;
import apiParts.skelethon.requests.search.SuccessfulSearchRequester;
import apiParts.specs.RequestSpecs;
import apiParts.specs.ResponseSpecs;

import java.time.OffsetDateTime;

import static apiParts.utils.DateTimeUtils.OPENMRS_REQUEST_DATE_TIME;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Steps for procedure tests (emrapi module): create / read procedures and values for test cases.
 */
public class ProcedureSteps {
    // Any duration is handled by the same server logic
    private static final int MIN_DURATION = 1;
    private static final int MAX_DURATION = 10;

    private ProcedureSteps() {
    }

    // POST /procedure, also used by @CreateProcedure
    public static ProcedureResponse createProcedure(CreateProcedureRequest request) {
        return new SuccessfulCrudRequester<ProcedureResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.PROCEDURE_POST,
                ResponseSpecs.requestReturnsCreated())
                .create(request);
    }

    // GET /procedure/{uuid}; deleted (voided) procedure is returned too
    public static ProcedureResponse getProcedure(String procedureUUID) {
        var procedure = new SuccessfulCrudRequester<ProcedureResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.PROCEDURE_GET,
                ResponseSpecs.requestReturnsOk()
        )
                .get(procedureUUID);
        assertThat(procedure.getUuid())
                .as("precondition: GET /procedure/{uuid} returns requested procedure")
                .isEqualTo(procedureUUID);
        return procedure;
    }

    // GET /procedure?patient={uuid}&v=full - not deleted procedures of the patient
    public static SearchResult<ProcedureResponse> getPatientProcedures(String patientUUID) {
        return getPatientProcedures(patientUUID, false);
    }

    // includeAll = true - deleted (voided) procedures too
    public static SearchResult<ProcedureResponse> getPatientProcedures(String patientUUID, boolean includeAll) {
        return new SuccessfulSearchRequester<ProcedureResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.PROCEDURES_GET,
                ResponseSpecs.requestReturnsOk()
        )
                .search(ProcedureSearchParams.builder()
                        .patient(patientUUID)
                        .representation(GetParams.FULL)
                        .includeAll(includeAll)
                        .build());
    }

    // startDateTime of the request, e.g. to build other dates of a case relative to it
    public static OffsetDateTime startDateTimeOf(CreateProcedureRequest request) {
        return OffsetDateTime.parse(request.getStartDateTime(), OPENMRS_REQUEST_DATE_TIME);
    }

    public static int randomDuration() {
        return RandomModelGenerator.randomInt(MIN_DURATION, MAX_DURATION);
    }
}
