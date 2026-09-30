package common.extensions;

import apiParts.models.encounter.EncounterResponse;
import apiParts.steps.AdminSteps;
import common.annotations.CreateOrder;
import common.storages.SessionStorage;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.Optional;

public class CreateOrderExtension implements BeforeEachCallback, AfterEachCallback {

    @Override
    public void beforeEach(ExtensionContext context) {
        SessionStorage.clearOrders();

        Optional<CreateOrder> annotation = ExtensionUtils.findAnnotation(context, CreateOrder.class);
        if (annotation.isEmpty()) {
            return;
        }

        String patientUUID = ExtensionUtils.requirePatientUuid(CreateOrder.class);
        EncounterResponse encounter = switch (annotation.get().value()) {
            case DRUG -> AdminSteps.createDrugOrderEncounter(patientUUID);
            case LAB -> AdminSteps.createLabOrderEncounter(patientUUID);
        };
        SessionStorage.addOrderEncounter(encounter);
    }

    @Override
    public void afterEach(ExtensionContext context) {
        SessionStorage.clearOrders();
    }
}
