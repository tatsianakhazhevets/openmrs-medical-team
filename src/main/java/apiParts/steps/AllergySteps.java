package apiParts.steps;

import apiParts.models.GetParams;
import apiParts.models.allergy.AllergyResponse;
import apiParts.models.patient.GetPatientResponse;
import apiParts.skelethon.endpoints.Endpoint;
import apiParts.skelethon.requests.nestedCrud.NestedCrudRequester;
import apiParts.skelethon.requests.nestedCrud.SuccessfulNestedCrudRequester;
import apiParts.specs.RequestSpecs;
import apiParts.specs.ResponseSpecs;

public class AllergySteps {

    public static GetPatientResponse getPatientAllergyNegative(String patientUuid) {

        new NestedCrudRequester(
                RequestSpecs.adminSpec(),
                Endpoint.PATIENT_GET_ALLERGY_NESTED,
                ResponseSpecs.requestReturnsNoContent())
                .get(patientUuid);

        return null;
    }

    public static AllergyResponse getPatientAllergyPositive(String patientUuid, String allergyUuid) {
        return new SuccessfulNestedCrudRequester<AllergyResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.PATIENT_ALLERGY_NESTED,
                ResponseSpecs.requestReturnsOk())
                .get(patientUuid, allergyUuid,
                        GetParams.builder().v(GetParams.FULL).build().toQueryParams());
    }
}