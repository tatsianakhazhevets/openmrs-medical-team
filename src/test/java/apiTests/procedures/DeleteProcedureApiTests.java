package apiTests.procedures;

import apiParts.assertions.ModelAssertions;
import apiParts.assertions.ProcedureAssertions;
import apiParts.models.BaseModel;
import apiParts.models.procedure.CreateProcedureRequest;
import apiParts.models.procedure.GetProceduresResponse;
import apiParts.models.procedure.ProcedureResponse;
import apiParts.skelethon.endpoints.Endpoint;
import apiParts.skelethon.requests.common.CrudRequester;
import apiParts.skelethon.requests.common.SuccessfulCrudRequester;
import apiParts.specs.RequestSpecs;
import apiParts.specs.ResponseSpecs;
import apiParts.steps.AdminSteps;
import apiTests.BaseTest;
import common.annotations.CreatePatient;
import common.annotations.CreateProcedure;
import common.storages.SessionStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

// DELETE /procedure/{uuid} is a soft delete (procedure is voided): hidden from search, still returned by uuid and with includeAll=true.
@CreatePatient
@CreateProcedure
public class DeleteProcedureApiTests extends BaseTest {
    private String patientUUID;
    private CreateProcedureRequest createRequest;
    private ProcedureResponse procedure;

    // Precondition: patient with one valid procedure (@CreatePatient, @CreateProcedure), returned by search
    @BeforeEach
    void setUp() {
        patientUUID = SessionStorage.getPatient().getUuid();
        createRequest = AdminSteps.procedureRequest(patientUUID);
        procedure = SessionStorage.getProcedure();

        assertThat(ProcedureAssertions.uuidsOf(getPatientProcedures(false)))
                .as("precondition: patient has created procedure")
                .isEqualTo(Set.of(procedure.getUuid()));
    }

    @Test
    public void adminCanDeleteProcedure() {
        new SuccessfulCrudRequester<BaseModel>(
                RequestSpecs.adminSpec(),
                Endpoint.PROCEDURE_DELETE,
                ResponseSpecs.requestReturnsNoContent()
        )
                .delete(procedure.getUuid());

        softly.assertThat(getPatientProcedures(false).getResults())
                .as("deleted procedure is not returned by search")
                .isEmpty();
        softly.assertThat(ProcedureAssertions.uuidsOf(getPatientProcedures(true)))
                .as("deleted procedure is returned by search with includeAll=true (soft delete)")
                .isEqualTo(Set.of(procedure.getUuid()));

        var deletedProcedure = getProcedure(procedure.getUuid());
        softly.assertThat(deletedProcedure.getVoided())
                .as("deleted procedure is marked as voided")
                .isTrue();
        ModelAssertions.assertMatchesExpected(softly,
                deletedProcedure,
                ProcedureAssertions.expectedProcedureOf(createRequest),
                "data of deleted procedure is kept");
    }

    @Test
    public void adminCannotDeleteNonExistentProcedure() {
        new CrudRequester(
                RequestSpecs.adminSpec(),
                Endpoint.PROCEDURE_DELETE,
                ResponseSpecs.requestReturnsNotFound()
        )
                .delete(UUID.randomUUID().toString());

        softly.assertThat(ProcedureAssertions.uuidsOf(getPatientProcedures(false)))
                .as("existing procedure is not affected")
                .isEqualTo(Set.of(procedure.getUuid()));
    }

    @Test
    public void unauthorizedUserCannotDeleteProcedure() {
        new CrudRequester(
                RequestSpecs.unAuthSpec(),
                Endpoint.PROCEDURE_DELETE,
                ResponseSpecs.requestReturnsUnauthorized()
        )
                .delete(procedure.getUuid());

        softly.assertThat(getProcedure(procedure.getUuid()).getVoided())
                .as("procedure is not deleted (not voided)")
                .isFalse();
        softly.assertThat(ProcedureAssertions.uuidsOf(getPatientProcedures(false)))
                .as("procedure is still returned by search")
                .isEqualTo(Set.of(procedure.getUuid()));
    }

    // ======== HELPERS ========
    private ProcedureResponse getProcedure(String uuid) {
        return new SuccessfulCrudRequester<ProcedureResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.PROCEDURE_GET,
                ResponseSpecs.requestReturnsOk()
        )
                .get(uuid);
    }

    private GetProceduresResponse getPatientProcedures(boolean includeAll) {
        return new SuccessfulCrudRequester<GetProceduresResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.PROCEDURES_GET,
                ResponseSpecs.requestReturnsOk()
        )
                .get(Map.of("patient", patientUUID, "v", "full", "includeAll", includeAll));
    }
}
