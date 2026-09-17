package apiParts.models.order;

import apiParts.models.encounter.Ref;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Drug order from GET /order?t=drugorder&v=full.
 * Nested resources are full objects in v=full, but only uuid + display are mapped (see {@link Ref}).
 */
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
    private String urgency;
    private String dosingType;
    private String dateActivated;
    private String dateStopped;
    private String autoExpireDate;      // calculated by server from duration + durationUnits

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
}
