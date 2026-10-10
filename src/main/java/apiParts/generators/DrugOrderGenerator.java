package apiParts.generators;

import apiParts.models.Location;
import apiParts.models.encounter.CreateEncounterRequest;
import apiParts.models.order.DiscontinueDrugOrderRequest;
import apiParts.models.order.Drug;
import apiParts.models.order.DrugOrder;

import java.util.List;
import java.util.Map;

public final class DrugOrderGenerator {
    private DrugOrderGenerator() {
    }

    public static DrugOrder generateDrugOrder() {
        return RandomModelGenerator.generate(DrugOrder.class);
    }

    public static DrugOrder generateUpcomingDrugOrder() {
        return RandomModelGenerator.generate(
                DrugOrder.class,
                Map.of(
                        "urgency", DrugOrder.URGENCY_ON_SCHEDULED_DATE,
                        "scheduledDate", RandomModelGenerator.futureDateTime()
                ));
    }

    public static CreateEncounterRequest generateEncounterRequest(DrugOrder order) {
        return RandomModelGenerator.generate(
                CreateEncounterRequest.class,
                Map.of(
                        "location", Location.OUTPATIENT_CLINIC,
                        "orders", List.of(order)
                ));
    }

    public static CreateEncounterRequest generateDiscontinueEncounterRequest(
            String previousOrderUUID, Drug drug) {
        DiscontinueDrugOrderRequest discontinueOrder = RandomModelGenerator.generate(
                DiscontinueDrugOrderRequest.class,
                Map.of(
                        "previousOrder", previousOrderUUID,
                        "drug", drug
                ));
        return RandomModelGenerator.generate(
                CreateEncounterRequest.class,
                Map.of(
                        "location", Location.OUTPATIENT_CLINIC,
                        "orders", List.of(discontinueOrder)
                ));
    }
}
