package apiParts.models.patient;

import apiParts.models.BaseModel;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class IdentifierResponse extends BaseModel {
    private String display;
    private String uuid;
    private String identifier;
    private IdentifiersType identifierType;
    private LocationResponse location;
    private Boolean preferred;
    private Boolean voided;
    private List<Object> links;
    private String resourceVersion;
}