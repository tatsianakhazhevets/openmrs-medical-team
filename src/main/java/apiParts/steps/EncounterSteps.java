package apiParts.steps;

import apiParts.models.GetParams;
import apiParts.models.euncouterTest.EncounterListResponse;
import apiParts.models.euncouterTest.EncounterTestResponse;
import apiParts.models.euncouterTest.GetEncounterResponse;
import apiParts.skelethon.endpoints.Endpoint;
import apiParts.skelethon.requests.crud.SuccessfulCrudRequester;
import apiParts.specs.RequestSpecs;
import apiParts.specs.ResponseSpecs;

public class EncounterSteps {

    public static EncounterListResponse getPatientEncounters(String patientUuid) {
        return new SuccessfulCrudRequester<EncounterListResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.ENCOUNTER_SEARCH_GET,
                ResponseSpecs.requestReturnsOk())
                .get(GetParams.builder().patient(patientUuid).v(GetParams.DEFAULT).build().toQueryParams());
    }

    public static GetEncounterResponse getEncounter(String encounterUuid) {
        return new SuccessfulCrudRequester<GetEncounterResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.ENCOUNTER_RETRIEVE,
                ResponseSpecs.requestReturnsOk())
                .get(encounterUuid, GetParams.builder().v(GetParams.FULL).build().toQueryParams());
    }
}