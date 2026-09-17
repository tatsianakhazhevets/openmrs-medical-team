package apiParts.skelethon.interfaces;

import apiParts.models.BaseModel;

import java.util.Map;

public interface CrudEndpoint {
    Object create(BaseModel model);

    Object create();

    Object get(int id);

    Object get();

    Object get(Map<String, ?> queryParams);

    Object get(String uuid);

    Object update(int id, BaseModel model);

    Object update(String uuid, BaseModel model);
    Object delete(int id);

    Object delete(String uuid);

    Object delete(String uuid, Map<String, ?> queryParams);
}