package apiParts.skelethon.interfaces;

import apiParts.models.BaseModel;
import io.restassured.response.ValidatableResponse;

import java.util.Map;

/**
 * Contract of a nested CRUD resource: one that cannot be addressed by a single uuid,
 * because it always belongs to a parent.
 *
 *   /patient/{parentUuid}/allergy/{childUuid}
 *            ^^^^^^^^^^^^         ^^^^^^^^^^
 *            owner                resource itself
 *
 * The only difference from {@link CrudEndpoint} is the number of uuids required
 * to address the resource. The path shape lives in
 * {@link apiParts.skelethon.endpoints.Endpoint} as a template with a {parentUuid}
 * placeholder; a test passes values only, never path fragments.
 *
 * Type parameter R is the return type: ValidatableResponse for the raw requester,
 * the response model for the Successful wrapper. That keeps Object and casts
 * out of the contract. delete() always returns ValidatableResponse: there is
 * nothing to deserialize on 204 No Content.
 */
public interface NestedCrudEndpoint<R> {

    R create(String parentUuid, BaseModel model);

    R get(String parentUuid, String childUuid);

    R get(String parentUuid, String childUuid, Map<String, ?> queryParams);

    R update(String parentUuid, String childUuid, BaseModel model);

    ValidatableResponse delete(String parentUuid, String childUuid);

    ValidatableResponse delete(String parentUuid, String childUuid, Map<String, ?> queryParams);
}
