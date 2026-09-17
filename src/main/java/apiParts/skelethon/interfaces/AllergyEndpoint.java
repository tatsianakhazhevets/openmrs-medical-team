package apiParts.skelethon.interfaces;

import apiParts.models.BaseModel;

public interface AllergyEndpoint {
    Object create(String patientUuid, BaseModel model);

    Object get(String patientUuid, String allergyUuid);

    Object update(String patientUuid, String allergyUuid, BaseModel model);

    Object delete(String patientUuid, String allergyUuid);
}