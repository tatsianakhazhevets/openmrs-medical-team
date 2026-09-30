package apiParts.models.encounter;

import apiParts.models.BaseModel;
import apiParts.models.Ref;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Who created / last changed the resource, returned by OpenMRS for {@code ?v=full} only.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuditInfo extends BaseModel {
    private Ref creator;
    private String dateCreated;
    private Ref changedBy;
    private String dateChanged;
}
