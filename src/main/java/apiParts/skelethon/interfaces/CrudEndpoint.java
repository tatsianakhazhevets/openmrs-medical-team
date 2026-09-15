package apiParts.skelethon.interfaces;

import apiParts.models.BaseModel;

public interface CrudEndpoint {
    Object create(BaseModel model);
    Object create();
    Object get(int id);
    Object get();
    Object update(int id, BaseModel model);
    Object delete(int id);
}