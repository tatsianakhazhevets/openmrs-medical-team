package apiParts.skelethon.interfaces;

import apiParts.models.BaseModel;

public interface EncounterEndpoint {
    Object create(BaseModel model);

    Object get(String encounterUuid);

    Object update(String encounterUuid, BaseModel model);

    Object delete(String encounterUuid);
}