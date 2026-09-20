package apiParts.generators;

import apiParts.models.patient.GetIdentifierResponse;
import apiParts.skelethon.endpoints.Endpoint;
import apiParts.skelethon.requests.common.SuccessfulCrudRequester;
import apiParts.specs.RequestSpecs;
import apiParts.specs.ResponseSpecs;

public class IdentifierGenerator {

    public static String generate(String identifierTypeUuid) {

        GetIdentifierResponse response =
                new SuccessfulCrudRequester<GetIdentifierResponse>(
                        RequestSpecs.adminSpec(),
                        Endpoint.IDENTIFIER_GET,
                        ResponseSpecs.requestReturnsCreated())
                        .create();

        return response.getIdentifier();
    }
}