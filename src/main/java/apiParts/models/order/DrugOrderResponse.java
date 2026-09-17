package apiParts.models.order;

import apiParts.models.encounter.Ref;
import apiParts.utils.DateTimeUtils;
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
public class DrugOrderResponse {
    private String uuid;
    private String orderNumber;         // e.g. "ORD-89"
    private String display;             // e.g. "(NEW) Aspirin 325mg: 3.0 Tablet Oral Once daily"
    private String type;                // "drugorder"
    private String action;
    private String urgency;             // ROUTINE or ON_SCHEDULED_DATE (Upcoming Medications)
    private String dosingType;
    private String dateActivated;
    private String scheduledDate;       // set only for urgency=ON_SCHEDULED_DATE (Upcoming Medications)
    private String dateStopped;         // set once the order is discontinued (Past Medications)
    private String autoExpireDate;      // calculated by server from duration + durationUnits

    private FulfillerStatus fulfillerStatus;   // testorder only, set via POST /order/{uuid}/fulfillerdetails
    private String fulfillerComment;           // testorder only

    private Ref patient;
    private Ref encounter;
    private Ref orderer;
    private Ref orderType;              // "Drug Order"
    private Ref careSetting;
    private Ref concept;
    private Ref drug;
    private Ref doseUnits;
    private Ref route;
    private Ref frequency;
    private Ref quantityUnits;
    private Ref durationUnits;

    private Double dose;
    private Double quantity;
    private Integer numRefills;
    private Integer duration;
    private Boolean asNeeded;
    private String orderReasonNonCoded;
    private String dosingInstructions;

    // Server converts offset to UTC (-04:00 -> +0000): normalize to Instant on deserialization,
    // so the same moment sent (request) and returned (response) in different offsets is equal in comparison.
    // Builder bypasses setters - expected value is normalized the same way in OrderAssertions.
    public void setScheduledDate(String scheduledDate) {
        this.scheduledDate = DateTimeUtils.toInstantString(scheduledDate);
    }
}
