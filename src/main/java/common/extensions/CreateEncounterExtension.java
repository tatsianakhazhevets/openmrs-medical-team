package common.extensions;

import apiParts.models.encounter.CreateEncounterResponse;
import apiParts.steps.AdminSteps;
import common.annotations.CreateEncounter;
import common.storages.SessionStorage;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.Optional;

public class CreateEncounterExtension implements BeforeEachCallback, AfterEachCallback {

    @Override
    public void beforeEach(ExtensionContext context) {
        SessionStorage.clearEncounters();

        Optional<CreateEncounter> annotation =
                ExtensionUtils.findAnnotation(context, CreateEncounter.class);

        if (annotation.isEmpty()) {
            return;
        }

        String patientUUID = ExtensionUtils.requirePatientUuid(CreateEncounter.class);

        CreateEncounterResponse encounter = switch (annotation.get().value()) {
            case VITALS -> AdminSteps.createVitalsEncounter(patientUUID);
            case ORDER -> throw new IllegalArgumentException(
                    "Use @CreateOrder for ORDER encounter"
            );
        };

        SessionStorage.addEncounter(encounter);
    }

    @Override
    public void afterEach(ExtensionContext context) {
        SessionStorage.clearEncounters();
    }
}