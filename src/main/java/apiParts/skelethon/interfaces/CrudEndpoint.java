package apiParts.skelethon.interfaces;

import apiParts.models.BaseModel;

import java.util.Map;

public interface CrudEndpoint {
    Object create(BaseModel model);
    Object create();
    Object get(int id);
    Object get();
    Object get(Map<String, ?> queryParams);
    Object update(int id, BaseModel model);
    Object delete(int id);
}