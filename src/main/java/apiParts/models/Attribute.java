package apiParts.models;

import apiParts.generators.StringGeneratingRule;
import apiParts.models.visit.VisitAttributeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Attribute {
    private VisitAttributeType attributeType;
    @StringGeneratingRule(regex = "POLICY-[0-9]{5}")
    private String value;
}
