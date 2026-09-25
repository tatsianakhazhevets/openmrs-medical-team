package apiParts.models.order;

import apiParts.models.BaseModel;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// POST /order/{orderUuid}/fulfillerdetails/: laboratory-side status of a test order
// (started processing / results entered / declined), set independently of the order itself.
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FulfillerDetailsRequest extends BaseModel {
    private FulfillerStatus fulfillerStatus;
    private String fulfillerComment;
}
