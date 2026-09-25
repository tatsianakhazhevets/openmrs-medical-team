package apiParts.models.euncouterTest;

import apiParts.models.BaseModel;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuditInfoResponse extends BaseModel {
    private AuditUserResponse creator;
    private String dateCreated;
    private AuditUserResponse changedBy;
    private String dateChanged;
}