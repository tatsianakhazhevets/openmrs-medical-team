package apiParts.skelethon.interfaces;

import apiParts.models.BaseModel;

import java.util.Map;

public interface IdentifierEndpoint {
    Object get(String patientUuid, String identifierUuid);

    Object update(String patientUuid, String identifierUuid, BaseModel model);

    Object delete(String patientUuid, String identifierUuid, Map<String, ?> queryParams);
}