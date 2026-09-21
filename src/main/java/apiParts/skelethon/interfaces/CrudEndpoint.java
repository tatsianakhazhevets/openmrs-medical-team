package apiParts.skelethon.interfaces;

import apiParts.models.BaseModel;

import java.util.Map;

public interface CrudEndpoint {
    Object create(BaseModel model);

    Object create();

    Object get(String uuid);

    Object get(String uuid, Map<String, ?> queryParams);

    Object update(String uuid, BaseModel model);

    Object delete(String uuid);

    Object delete(String uuid, Map<String, ?> queryParams);
}
