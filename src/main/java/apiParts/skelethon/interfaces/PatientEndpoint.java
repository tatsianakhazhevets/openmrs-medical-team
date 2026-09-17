package apiParts.skelethon.interfaces;

import apiParts.models.BaseModel;

import java.util.Map;

public interface PatientEndpoint {
    Object create(String patientUuid, BaseModel model);
    Object update(String uuid, BaseModel model);
    Object delete(String uuid, Map<String, ?> queryParams);
}