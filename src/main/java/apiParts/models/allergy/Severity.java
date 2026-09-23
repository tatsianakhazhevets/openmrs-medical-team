package apiParts.models.allergy;

import apiParts.generators.EnumGeneratingRule;
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
public class Severity extends BaseModel {
    @EnumGeneratingRule(enumClass = SeverityUuid.class, valueMethod = "getUuid")
    private String uuid;
}