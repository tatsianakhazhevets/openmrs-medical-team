package apiParts.models.patient;

import apiParts.generators.BooleanGeneratingRule;
import apiParts.generators.EnumGeneratingRule;
import apiParts.generators.IdentifierGeneratingRule;
import apiParts.generators.StringGeneratingRule;
import apiParts.models.BaseModel;
import apiParts.models.Location;
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
public class PatientIdentifierRequest extends BaseModel {
    @IdentifierGeneratingRule
    private String identifier;

    @EnumGeneratingRule(enumClass = IdentifierType.class, valueMethod = "getUuid")
    private String identifierType;

    @EnumGeneratingRule(enumClass = Location.class, valueMethod = "getUuid")
    private String location;

    @BooleanGeneratingRule(true)
    private Boolean preferred;
}