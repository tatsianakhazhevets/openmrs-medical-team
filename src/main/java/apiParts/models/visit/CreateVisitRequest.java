package apiParts.models.visit;

import apiParts.generators.*;
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

    @PatientUuidGeneratingRule
    private String patient;

    private VisitType visitType;
    private VisitLocation location;

    @DateTimeGeneratingRule(minutesFromNow = 60)
    private String startDatetime;

    @DateTimeGeneratingRule(
            baseField = "startDatetime",
            minutesFromBase = 30
    )
    private String stopDatetime;

    @StringGeneratingRule(regex = "[A-Za-z ]{5,100}")
    private String indication;

    @IgnoreGeneratingRule
    private List<String> encounters;

    @CollectionGeneratingRule(minSize = 1, maxSize = 1)
    private List<Attribute> attributes;
}
