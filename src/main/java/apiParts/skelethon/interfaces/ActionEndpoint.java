package apiParts.skelethon.interfaces;

import apiParts.models.BaseModel;

/**
 * Contract of an action endpoint: a command performed on an existing resource,
 * addressed as /resource/{resourceUuid}/{action}.
 *
 *   POST /appointments/{uuid}/status-change
 *   POST /order/{uuid}/fulfillerdetails/
 *
 * Why this is not NestedCrud: the last path segment is a literal action name,
 * not the uuid of a child resource. Nothing is created, read, updated or deleted
 * in the CRUD sense - a command is executed. Calling it create() would misname it.
 *
 * The path lives in {@link apiParts.skelethon.endpoints.Endpoint} as a template with
 * a {resourceUuid} placeholder; a test passes the uuid and the command body only.
 */
public interface ActionEndpoint<R> {

    R perform(String resourceUuid, BaseModel body);
}
