package apiParts.models.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderSearchParams {

    private String patient;
    private String careSetting;
    private Integer limit;
    private Integer startIndex;
    private String representation;
}