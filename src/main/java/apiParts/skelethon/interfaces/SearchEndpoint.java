package apiParts.skelethon.interfaces;

import apiParts.models.BaseModel;
import apiParts.models.search.SearchParams;

/**
 * Contract of a search endpoint: one that answers with a COLLECTION rather than
 * a single entity.
 *
 * Two transports, because the project's API uses both:
 *  - search()       GET  /entity?patient=...&v=full   (orders, procedures, patients, obs)
 *  - searchByBody() POST /entity/search + body        (appointments)
 */
public interface SearchEndpoint<R> {

    R search(SearchParams params);

    R searchByBody(BaseModel body);
}
