package apiParts.skelethon.interfaces;

import apiParts.models.BaseModel;
import io.restassured.response.Response;

public interface CrudEndpoint {
    Object create(BaseModel model);
    Object get(int id);
    Object get();
    Object update(int id, BaseModel model);
    Object delete(int id);
}