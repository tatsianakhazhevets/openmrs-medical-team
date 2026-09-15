package apiParts.assertions;

import apiParts.models.HasUuid;
import apiParts.models.encounter.CreateEncounterRequest;
import apiParts.models.encounter.CreateEncounterResponse;
import apiParts.models.encounter.Ref;
import apiParts.models.order.DrugOrder;
import apiParts.models.order.DrugOrderResponse;
import apiParts.models.order.GetOrderResponse;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Builds expected drug orders from request (for {@link ModelAssertions#assertMatchesExpected})
 * and extracts order uuids from POST / GET responses.
 */
public class OrderAssertions {

    private OrderAssertions() {
    }

    // what GET /order?t=drugorder&v=full should return for orders sent in POST /encounter
    public static List<DrugOrderResponse> expectedOrdersOf(CreateEncounterRequest request) {
        return request.getOrders().stream()
                .map(OrderAssertions::expectedOf)
                .toList();
    }

    // sort key for ModelAssertions.assertListMatchesExpected
    public static String drugUuidOf(DrugOrderResponse order) {
        return order.getDrug() == null ? null : order.getDrug().getUuid();
    }

    // order uuids returned by POST /encounter
    public static Set<String> uuidsOf(CreateEncounterResponse response) {
        return response.getOrders().stream()
                .map(Ref::getUuid)
                .collect(Collectors.toCollection(TreeSet::new));
    }

    // order uuids returned by GET /order
    public static Set<String> uuidsOf(GetOrderResponse response) {
        return response.getResults().stream()
                .map(DrugOrderResponse::getUuid)
                .collect(Collectors.toCollection(TreeSet::new));
    }

    // ======== HELPERS ========
    // null fields of request stay null in expected and are not checked
    private static DrugOrderResponse expectedOf(DrugOrder order) {
        return DrugOrderResponse.builder()
                .type(order.getType())
                .action(order.getAction())
                .dosingType(order.getDosingType())
                .patient(ref(order.getPatient()))
                .careSetting(ref(order.getCareSetting()))
                .orderer(ref(order.getOrderer()))
                .drug(ref(order.getDrug()))
                .concept(ref(expectedConcept(order)))
                .dose(order.getDose())
                .doseUnits(ref(order.getDoseUnits()))
                .route(ref(order.getRoute()))
                .frequency(ref(order.getFrequency()))
                .quantity(order.getQuantity())
                .quantityUnits(ref(order.getQuantityUnits()))
                .numRefills(order.getNumRefills())
                .duration(order.getDuration())
                .durationUnits(ref(order.getDurationUnits()))
                .asNeeded(order.getAsNeeded())
                .dosingInstructions(order.getDosingInstructions())
                .orderReasonNonCoded(order.getOrderReasonNonCoded())
                .build();
    }

    // concept is optional in request: server takes it from drug
    private static String expectedConcept(DrugOrder order) {
        if (order.getConcept() != null || order.getDrug() == null) {
            return order.getConcept();
        }
        return order.getDrug().getConceptUuid();
    }

    private static Ref ref(HasUuid value) {
        return value == null ? null : Ref.of(value.getUuid());
    }

    private static Ref ref(String uuid) {
        return uuid == null ? null : Ref.of(uuid);
    }
}
