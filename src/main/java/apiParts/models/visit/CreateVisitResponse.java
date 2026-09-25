package apiParts.models.visit;

import apiParts.models.BaseModel;
import apiParts.models.encounter.Ref;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateVisitResponse extends BaseModel {
    private String uuid;
    private Ref patient;
    private Ref visitType;
    private Ref location;
    private String startDatetime;
    private String stopDatetime;
    private List<Ref> encounters;
    private List<Ref> attributes;
}
