package apiParts.skelethon.interfaces;

import io.restassured.response.Response;

public interface CrudEndpoint {
    Response create();
    Response get();
    Response update();
    Response delete();
}