package apiParts.models.euncouterTest;

import apiParts.models.BaseModel;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class LocationTagResponse extends BaseModel {
    private String uuid;
    private String display;
    private List<EncounterLinkResponse> links;
}