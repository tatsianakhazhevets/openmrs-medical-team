package apiParts.models.patient;

import java.util.List;

import apiParts.models.BaseModel;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class IdentifiersType extends BaseModel {
    private String uuid;
    private String display;
    private List<Object> links;
}