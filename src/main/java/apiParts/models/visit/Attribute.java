package apiParts.models.visit;

import apiParts.generators.GeneratingRule;
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
    @GeneratingRule(regex = "POLICY-[0-9]{5}")
    private String value;
}
