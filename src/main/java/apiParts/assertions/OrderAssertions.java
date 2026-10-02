package apiParts.assertions;

import apiParts.models.HasUuid;
import apiParts.models.Ref;
import apiParts.models.encounter.CreateEncounterRequest;
import apiParts.models.encounter.EncounterResponse;
import apiParts.models.order.DrugOrder;
import apiParts.models.order.DrugOrderResponse;
import apiParts.models.order.TestOrder;
import apiParts.utils.DateTimeUtils;

import java.time.Instant;
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
                .map(DrugOrder.class::cast)
                .map(OrderAssertions::expectedOf)
                .toList();
    }

    // sort key for ModelAssertions.assertListMatchesExpected
    public static String drugUuidOf(DrugOrderResponse order) {
        return order.getDrug() == null ? null : order.getDrug().getUuid();
    }

    // order uuids returned by POST /encounter
    public static Set<String> uuidsOf(EncounterResponse response) {
        return response.getOrders().stream()
                .map(Ref::getUuid)
                .collect(Collectors.toCollection(TreeSet::new));
    }

    // order uuids from a search result (SuccessfulSearchRequester already unwrapped {"results": [...]})
    public static Set<String> uuidsOf(List<DrugOrderResponse> results) {
        return results.stream()
                .map(DrugOrderResponse::getUuid)
                .collect(Collectors.toCollection(TreeSet::new));
    }

    // the single drug order in a search result (Active/Upcoming/Past Medications tests)
    public static DrugOrderResponse onlyOrderOf(List<DrugOrderResponse> results) {
        if (results.size() != 1) {
            throw new AssertionError("expected exactly one drug order, but GET /order returned " + results.size());
        }
        return results.get(0);
    }

    public static void assertPersistedOrderReferences(CreateEncounterRequest request,
                                                      EncounterResponse created,
                                                      EncounterResponse persisted,
                                                      String description) {
        SoftlyContext.get().assertThat(persisted.getOrders())
                .extracting(Ref::getUuid)
                .as(description)
                .hasSize(request.getOrders().size())
                .containsExactlyInAnyOrderElementsOf(
                        created.getOrders().stream().map(Ref::getUuid).toList());
    }

    public static void assertDrugOrderSaved(CreateEncounterRequest request,
                                            EncounterResponse encounter,
                                            List<DrugOrderResponse> orders,
                                            String description) {
        SoftlyContext.get().assertThat(encounter.getOrders())
                .as(description + " created on encounter")
                .hasSize(request.getOrders().size());
        SoftlyContext.get().assertThat(orders)
                .as(description + " returned for patient")
                .hasSize(request.getOrders().size());
        DrugOrderResponse saved = onlyOrderOf(orders);
        SoftlyContext.get().assertThat(saved.getUuid())
                .as(description + " uuid")
                .isEqualTo(encounter.getOrders().get(0).getUuid());
    }

    public static void assertLabOrderDetails(String patientUuid,
                                             TestOrder labOrder,
                                             CreateEncounterRequest request,
                                             EncounterResponse created,
                                             EncounterResponse persisted,
                                             DrugOrderResponse savedOrder) {
        assertPersistedOrderReferences(request, created, persisted,
                "lab order references persisted on encounter");
        SoftlyContext.get().assertThat(created.getObs())
                .as("lab encounter obs")
                .isEmpty();
        SoftlyContext.get().assertThat(created.getVoided())
                .as("lab encounter voided")
                .isFalse();
        SoftlyContext.get().assertThat(created.getOrders())
                .as("lab encounter orders")
                .hasSize(request.getOrders().size());
        SoftlyContext.get().assertThat(created.getOrders().get(0).getDisplay())
                .as("lab order display")
                .isNotBlank();
        SoftlyContext.get().assertThat(savedOrder.getPatient().getUuid())
                .as("saved lab order patient")
                .isEqualTo(patientUuid);
        SoftlyContext.get().assertThat(savedOrder.getConcept().getUuid())
                .as("saved lab order concept")
                .isEqualTo(labOrder.getConcept().getUuid());
    }

    public static void assertOrderAbsent(List<DrugOrderResponse> orders, String orderUuid, String description) {
        SoftlyContext.get().assertThat(orders)
                .as(description)
                .noneMatch(order -> order.getUuid().equals(orderUuid));
    }

    public static void assertOrderPresent(List<DrugOrderResponse> orders, String orderUuid, String description) {
        SoftlyContext.get().assertThat(orders)
                .as(description)
                .anyMatch(order -> order.getUuid().equals(orderUuid));
    }

    public static void assertActiveOrders(List<?> requests, List<DrugOrderResponse> responses) {
        SoftlyContext.get().assertThat(responses)
                .as("active drug orders saved for patient")
                .hasSize(requests.size());
        for (DrugOrderResponse saved : responses) {
            SoftlyContext.get().assertThat(saved.getUrgency())
                    .as("active order defaults to ROUTINE urgency")
                    .isEqualTo(DrugOrder.URGENCY_ROUTINE);
            SoftlyContext.get().assertThat(saved.getScheduledDate())
                    .as("active order has no scheduledDate")
                    .isNull();
            SoftlyContext.get().assertThat(saved.getDateStopped())
                    .as("active order is not stopped")
                    .isNull();
        }
    }

    public static void assertUpcomingOrder(DrugOrder request, List<DrugOrderResponse> responses) {
        SoftlyContext.get().assertThat(responses)
                .as("upcoming drug orders saved for patient")
                .hasSize(1);
        DrugOrderResponse saved = onlyOrderOf(responses);
        SoftlyContext.get().assertThat(saved.getUrgency())
                .as("upcoming order urgency")
                .isEqualTo(DrugOrder.URGENCY_ON_SCHEDULED_DATE);
        SoftlyContext.get().assertThat(saved.getScheduledDate())
                .as("upcoming order scheduledDate is in the future")
                .isNotNull()
                .matches(date -> Instant.parse(date).isAfter(Instant.now()), "is after now");
        SoftlyContext.get().assertThat(saved.getScheduledDate())
                .as("upcoming order scheduledDate matches the generated request")
                .isEqualTo(DateTimeUtils.toInstantString(request.getScheduledDate()));
        SoftlyContext.get().assertThat(saved.getDateStopped())
                .as("upcoming order is not stopped")
                .isNull();
    }

    public static void assertDiscontinuedOrderIsPast(CreateEncounterRequest discontinueRequest,
                                                     EncounterResponse discontinued,
                                                     String originalOrderUuid,
                                                     List<DrugOrderResponse> responses) {
        SoftlyContext.get().assertThat(discontinued.getOrders())
                .as("DISCONTINUE creates an order record")
                .hasSize(discontinueRequest.getOrders().size());
        String discontinueStubUuid = discontinued.getOrders().get(0).getUuid();
        SoftlyContext.get().assertThat(discontinueStubUuid)
                .as("DISCONTINUE creates a new order record, distinct from the original")
                .isNotEqualTo(originalOrderUuid);
        SoftlyContext.get().assertThat(responses)
                .as("excludeDiscontinueOrders hides the DISCONTINUE stub, original order remains")
                .hasSize(1)
                .first()
                .extracting(DrugOrderResponse::getUuid)
                .isEqualTo(originalOrderUuid);
        SoftlyContext.get().assertThat(onlyOrderOf(responses).getDateStopped())
                .as("original order is stopped once discontinued")
                .isNotNull();
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
                .urgency(order.getUrgency())
                // builder bypasses setters of DrugOrderResponse - date is normalized here the same way
                .scheduledDate(DateTimeUtils.toInstantString(order.getScheduledDate()))
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
