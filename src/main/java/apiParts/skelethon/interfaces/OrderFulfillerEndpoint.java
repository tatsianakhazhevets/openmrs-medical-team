package apiParts.skelethon.interfaces;

import apiParts.models.BaseModel;

// POST /order/{orderUuid}/fulfillerdetails/: nested path, not covered by CrudEndpoint's uuid-suffix convention
public interface OrderFulfillerEndpoint {
    Object updateFulfillerDetails(String orderUuid, BaseModel model);
}
