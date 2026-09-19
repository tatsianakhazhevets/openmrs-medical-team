package apiParts.models.search;

import java.util.List;

/**
 * Contract of an OpenMRS collection response: {"results": [...]}.
 *
 * Implemented by the existing response models (no behaviour added - only the
 * getter Lombok already generates), so the framework can pull the list out
 * generically instead of every test calling getResults() and streaming by hand.
 */
public interface SearchResponse<T> {

    List<T> getResults();
}
