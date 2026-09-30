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

    @GeneratingRule(strategy = GenerationStrategy.PATIENT_UUID)
    private String patient;

    private VisitType visitType;
    @GeneratingRule(enumClass = Location.class, enumValue = "MOBILE_CLINIC")
    private Location location;

    @GeneratingRule(strategy = GenerationStrategy.DATE_TIME, minutesFromNow = 60)
    private String startDatetime;

    @GeneratingRule(
            strategy = GenerationStrategy.DATE_TIME,
            baseField = "startDatetime",
            minutesFromBase = 30
    )
    private String stopDatetime;

    @GeneratingRule(regex = "[A-Za-z ]{5,100}")
    private String indication;

    @GeneratingRule(nullable = true)
    private List<String> encounters;

    @GeneratingRule(minSize = 1, maxSize = 1)
    private List<Attribute> attributes;
}
