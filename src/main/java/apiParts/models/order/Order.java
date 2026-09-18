package apiParts.models.order;

import apiParts.models.BaseModel;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Order extends BaseModel {

    private String uuid;
    private String orderNumber;
    private String accessionNumber;

    private ResourceReference patient;
    private ResourceReference concept;

    private String action;

    private ResourceReference careSetting;
    private ResourceReference previousOrder;

    private String dateActivated;
    private String scheduledDate;
    private String dateStopped;
    private String autoExpireDate;

    private ResourceReference encounter;
    private ResourceReference orderer;

    private FulfillerStatus fulfillerStatus;
    private String fulfillerComment;

    private ResourceReference orderReason;
    private String orderReasonNonCoded;

    private OrderType orderType;

    private String urgency;
    private String instructions;
    private String commentToFulfiller;
    private String display;

    private ResourceReference specimenSource;
    private String laterality;
    private String clinicalHistory;
    private ResourceReference frequency;

    private Integer numberOfRepeats;

    private List<Link> links;

    private String type;
    private String resourceVersion;
}