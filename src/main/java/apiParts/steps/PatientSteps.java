package apiParts.steps;

import apiParts.models.patient.GetPatientResponse;
import apiParts.models.patient.PatientSearchParams;
import apiParts.models.search.SearchResult;
import apiParts.skelethon.endpoints.Endpoint;
import apiParts.skelethon.requests.crud.SuccessfulCrudRequester;
import apiParts.skelethon.requests.search.SuccessfulSearchRequester;
import apiParts.specs.RequestSpecs;
import apiParts.specs.ResponseSpecs;

public class PatientSteps {

    public static SearchResult<GetPatientResponse> getPatients(String searchQuery) {
        SearchResult<GetPatientResponse> patients = new SuccessfulSearchRequester<GetPatientResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.PATIENT_SEARCH_GET,
                ResponseSpecs.requestReturnsOk())
                .search(PatientSearchParams.builder()
                        .query(searchQuery)
                        .representation(PatientSearchParams.DEFAULT_REPRESENTATION)
                        .limit(PatientSearchParams.DEFAULT_LIMIT)
                        .build());

        return patients;
    }

    public static GetPatientResponse getPatientNegative(String uuid) {
        GetPatientResponse getPatientResponse = new SuccessfulCrudRequester<GetPatientResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.PATIENT_GET,
                ResponseSpecs.requestReturnsNotFound())
                .get(uuid);

        return getPatientResponse;
    }

    public static GetPatientResponse getPatientPositive(String uuid) {
        GetPatientResponse getPatientResponse = new SuccessfulCrudRequester<GetPatientResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.PATIENT_GET,
                ResponseSpecs.requestReturnsOk())
                .get(uuid);

        return getPatientResponse;
    }
}