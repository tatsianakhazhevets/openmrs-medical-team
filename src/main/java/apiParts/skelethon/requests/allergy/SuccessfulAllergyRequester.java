package apiParts.skelethon.requests.allergy;

import apiParts.models.BaseModel;
import apiParts.skelethon.base_request.HttpRequest;
import apiParts.skelethon.endpoints.Endpoint;
import apiParts.skelethon.interfaces.AllergyEndpoint;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;

import java.util.Map;

public class SuccessfulAllergyRequester<T extends BaseModel> extends HttpRequest implements AllergyEndpoint {

    private AllergyRequester allergyRequester;

    public SuccessfulAllergyRequester(RequestSpecification requestSpecification, Endpoint endpoint, ResponseSpecification responseSpecification) {
        super(requestSpecification, endpoint, responseSpecification);
        this.allergyRequester = new AllergyRequester(requestSpecification, endpoint, responseSpecification);
    }

    @Override
    public T create(String patientUuid, BaseModel model) {
        return (T) allergyRequester.create(patientUuid, model).extract().as(endpoint.getResponseModel());
    }

    @Override
    public T get(String patientUuid, String allergyUuid) {
        return (T) allergyRequester
                .get(patientUuid, allergyUuid)
                .extract()
                .as(endpoint.getResponseModel());
    }

    @Override
    public T update(String patientUuid, String allergyUuid, BaseModel model) {
        return (T) allergyRequester
                .update(patientUuid, allergyUuid, model)
                .extract()
                .as(endpoint.getResponseModel());
    }

    @Override
    public Object delete(String patientUuid, String allergyUuid) {
        return allergyRequester.delete(
                patientUuid,
                allergyUuid);
    }
}