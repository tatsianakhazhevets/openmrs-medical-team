package apiParts.models.order;

// org.openmrs.Order.Action; the server returns name() as the "action" value
public enum OrderAction {
    NEW,
    REVISE,
    DISCONTINUE,
    RENEW
}
