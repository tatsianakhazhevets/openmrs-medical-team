package apiParts.models;

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
    private String value;
}
