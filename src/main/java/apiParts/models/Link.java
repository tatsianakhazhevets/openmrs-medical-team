package apiParts.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * HATEOAS link returned by OpenMRS next to every resource reference:
 * <pre>
 * { "rel": "self", "uri": "http://.../ws/rest/v1/location/44c3efb0", "resourceAlias": "location" }
 * </pre>
 * Not part of the business payload - present only so that responses deserialize
 * into the same {@link Ref} model no matter which endpoint returned them.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Link {
    private String rel;
    private String uri;
    private String resourceAlias;
}
