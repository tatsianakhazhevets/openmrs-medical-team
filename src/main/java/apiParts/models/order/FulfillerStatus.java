package apiParts.models.order;

// org.openmrs.Order.FulfillerStatus, used by POST /order/{uuid}/fulfillerdetails
// Default enum JSON serialization (name()) matches the values expected/returned by the server,
// so no @JsonValue mapping is needed here (unlike concept/uuid based enums, e.g. CareSetting).
public enum FulfillerStatus {
    RECEIVED,
    IN_PROGRESS,
    COMPLETED,
    DECLINED,
    EXCEPTION
}
