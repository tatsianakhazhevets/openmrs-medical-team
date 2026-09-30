package apiTests.procedures;

import apiParts.assertions.ModelAssertions;
import apiParts.generators.RandomModelGenerator;
import apiParts.models.errors.ProcedureErrorMessage;
import apiParts.models.procedure.CreateProcedureRequest;
import apiParts.models.procedure.ProcedureConcept;
import apiParts.models.procedure.ProcedureResponse;
import apiParts.skelethon.endpoints.Endpoint;
import apiParts.skelethon.requests.crud.CrudRequester;
import apiParts.skelethon.requests.crud.SuccessfulCrudRequester;
import apiParts.models.search.SearchResult;
import apiParts.models.procedure.ProcedureSearchParams;
import apiParts.skelethon.requests.search.SearchRequester;
import apiParts.skelethon.requests.search.SuccessfulSearchRequester;
import apiParts.specs.RequestSpecs;
import apiParts.specs.ResponseSpecs;
import apiParts.utils.Uuids;
import apiTests.BaseTest;
import common.annotations.CreatePatient;
import common.annotations.CreateProcedure;
import common.storages.SessionStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@CreatePatient
public class GetProcedureApiTests extends BaseTest {
    private String patientUUID;

    @BeforeEach
    void setUp() {
        patientUUID = SessionStorage.getPatient().getUuid();
    }

    @Test
    public void adminCanGetProcedureByUuid() {
        var request = RandomModelGenerator.generate(CreateProcedureRequest.class);

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
        ModelAssertions.assertThatModels(request, savedProcedure)
                .as("procedure returned by GET /procedure/{uuid}")
                .match();
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
            var request = RandomModelGenerator.generate(CreateProcedureRequest.class);
            request.setProcedureCoded(ProcedureConcept.values()[i].getUuid());
            requests.add(request);
            procedures.add(createProcedure(request));
        }

        var patientProcedures = getPatientProcedures();

        ModelAssertions.assertThatModels(requests, patientProcedures.results())
                .as("procedures returned for patient")
                .match();
        softly.assertThat(Uuids.of(patientProcedures.results()))
                .as("procedure uuids from GET match POST /procedure")
                .isEqualTo(Uuids.of(procedures));
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
        new SearchRequester(
                RequestSpecs.adminSpec(),
                Endpoint.PROCEDURES_GET,
                ResponseSpecs.requestReturnsBadRequestWithMessage(ProcedureErrorMessage.OPERATION_NOT_SUPPORTED)
        )
                .search(ProcedureSearchParams.builder()
                        .representation("full")
                        .build());
    }

    @Test
    @DisplayName("[known issue] admin cannot get procedures of non-existent patient (server returns 500)")
    public void adminCannotGetProceduresOfNonExistentPatient() {
        new SearchRequester(
                RequestSpecs.adminSpec(),
                Endpoint.PROCEDURES_GET,
                ResponseSpecs.requestReturnsBadRequestWithMessage(ProcedureErrorMessage.PATIENT_NOT_FOUND)
        )
                .search(ProcedureSearchParams.builder()
                        .patient(UUID.randomUUID().toString())
                        .representation("full")
                        .build());
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
        new SearchRequester(
                RequestSpecs.unAuthSpec(),
                Endpoint.PROCEDURES_GET,
                ResponseSpecs.requestReturnsUnauthorized()
        )
                .search(ProcedureSearchParams.builder()
                        .patient(patientUUID)
                        .representation("full")
                        .build());
    }

    // ======== HELPERS ========
    private ProcedureResponse createProcedure(CreateProcedureRequest request) {
        return new SuccessfulCrudRequester<ProcedureResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.PROCEDURE_POST,
                ResponseSpecs.requestReturnsCreated()
        )
                .create(request);
    }

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
}
