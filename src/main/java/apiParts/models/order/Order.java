package apiParts.models.order;

import apiParts.models.Link;
import apiParts.models.Ref;
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

    private Ref patient;
    private Ref concept;

    private String action;

    private Ref careSetting;
    private Ref previousOrder;

    private String dateActivated;
    private String scheduledDate;
    private String dateStopped;
    private String autoExpireDate;

    private Ref encounter;
    private Ref orderer;

    private FulfillerStatus fulfillerStatus;
    private String fulfillerComment;

    private Ref orderReason;
    private String orderReasonNonCoded;

    private OrderType orderType;

    private String urgency;
    private String instructions;
    private String commentToFulfiller;
    private String display;

    private Ref specimenSource;
    private String laterality;
    private String clinicalHistory;
    private Ref frequency;

    private Integer numberOfRepeats;

    private List<Link> links;

    private String type;
    private String resourceVersion;
}