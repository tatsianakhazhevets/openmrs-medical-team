package apiParts.steps;

import apiParts.models.GetParams;
import apiParts.models.search.SearchParams;
import apiParts.models.search.SearchResult;
import apiParts.models.visit.CreateVisitRequest;
import apiParts.models.visit.CreateVisitResponse;
import apiParts.models.visit.GetVisitResponse;
import apiParts.models.visit.VisitType;
import apiParts.skelethon.endpoints.Endpoint;
import apiParts.skelethon.requests.crud.SuccessfulCrudRequester;
import apiParts.skelethon.requests.search.SuccessfulSearchRequester;
import apiParts.specs.RequestSpecs;
import apiParts.specs.ResponseSpecs;

import java.util.List;
import java.util.Map;

public class VisitSteps {
    public static CreateVisitResponse createVisitWithRequiredFields(String patientUUID) {
        CreateVisitRequest request = CreateVisitRequest.builder().patient(patientUUID)
                .visitType(VisitType.FACILITY_VISIT)
                .build();

        return new SuccessfulCrudRequester<CreateVisitResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.VISIT_POST,
                ResponseSpecs.requestReturnsCreated()).create(request);
    }

    public static CreateVisitRequest visitRequest(String patientUUID, List<String> encounterUUIDs) {
        return CreateVisitRequest.builder()
                .patient(patientUUID)
                .visitType(VisitType.FACILITY_VISIT)
                .encounters(encounterUUIDs)
                .build();
    }
    public static void deleteVisit(String visitUUID) {
        new SuccessfulCrudRequester<CreateVisitResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.VISIT_DELETE,
                ResponseSpecs.requestReturnsNoContent()
        ).delete(visitUUID, Map.of("purge", true));
    }
    public static CreateVisitResponse createVisit(CreateVisitRequest request) {
        return new SuccessfulCrudRequester<CreateVisitResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.VISIT_POST,
                ResponseSpecs.requestReturnsCreated()
        ).create(request);
    }

    public static GetVisitResponse getVisit(String visitUUID) {
        return new SuccessfulCrudRequester<GetVisitResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.VISIT_GET,
                ResponseSpecs.requestReturnsOk()
        ).get(visitUUID, new GetParams(GetParams.FULL).toQueryParams());
    }



    public static SearchResult<GetVisitResponse> getVisits(String patientUUID) {
        SearchParams visits = () -> Map.<String, Object>of(
                "patient", patientUUID,
                "v", "custom");

        return new SuccessfulSearchRequester<GetVisitResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.VISITS_GET,
                ResponseSpecs.requestReturnsOk())
                .search(visits);
    }
}
