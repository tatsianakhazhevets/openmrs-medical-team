package apiParts.assertions;

import apiParts.models.encounter.Ref;
import apiParts.models.procedure.CreateProcedureRequest;
import apiParts.models.procedure.ProcedureResponse;
import apiParts.utils.DateTimeUtils;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Builds expected procedures from request (for {@link ModelAssertions#assertListMatchesExpected})
 * and extracts procedure uuids from POST / GET responses.
 */
public class ProcedureAssertions {

    private ProcedureAssertions() {
    }

    // what GET /procedure?patient={uuid}&v=full should return for procedure sent in POST /procedure
    public static List<ProcedureResponse> expectedProceduresOf(CreateProcedureRequest request) {
        return List.of(expectedProcedureOf(request));
    }

    // same for several procedures of one patient
    public static List<ProcedureResponse> expectedProceduresOf(List<CreateProcedureRequest> requests) {
        return requests.stream()
                .map(ProcedureAssertions::expectedProcedureOf)
                .toList();
    }

    // sort key for ModelAssertions.assertListMatchesExpected
    public static String procedureCodedUuidOf(ProcedureResponse procedure) {
        return procedure.getProcedureCoded() == null ? null : procedure.getProcedureCoded().getUuid();
    }

    // procedure uuid returned by POST /procedure
    public static Set<String> uuidsOf(ProcedureResponse response) {
        return new TreeSet<>(Set.of(response.getUuid()));
    }

    // procedure uuids returned by several POST /procedure
    public static Set<String> uuidsOf(List<ProcedureResponse> responses) {
        return responses.stream()
                .map(ProcedureResponse::getUuid)
                .collect(Collectors.toCollection(TreeSet::new));
    }

    // what POST /procedure and GET /procedure/{uuid} should return for the request
    // null fields of request stay null in expected and are not checked
    public static ProcedureResponse expectedProcedureOf(CreateProcedureRequest request) {
        return ProcedureResponse.builder()
                .patient(ref(request.getPatient()))
                .procedureCoded(ref(request.getProcedureCoded()))
                .procedureNonCoded(request.getProcedureNonCoded())
                .procedureType(ref(request.getProcedureType()))
                .bodySite(ref(request.getBodySite()))
                // builder bypasses setters of ProcedureResponse - dates are normalized here the same way
                .startDateTime(DateTimeUtils.toInstantString(request.getStartDateTime()))
                .endDateTime(DateTimeUtils.toInstantString(request.getEndDateTime()))
                .estimatedStartDate(request.getEstimatedStartDate())
                .status(ref(request.getStatus()))
                .notes(request.getNotes())
                .duration(request.getDuration())
                .durationUnit(ref(request.getDurationUnit()))
                .build();
    }

    // ======== HELPERS ========
    private static Ref ref(String uuid) {
        return uuid == null ? null : Ref.of(uuid);
    }
}
