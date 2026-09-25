package apiParts.models.visit;

import apiParts.models.*;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreateVisitRequest extends BaseModel {
    private String patient;
    private VisitType visitType;
    private VisitLocation location;
    private String startDatetime;
    private String stopDatetime;
    private String indication;
    private List<String> encounters;
    private List<Attribute> attributes;
}
