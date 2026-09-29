package common.extensions;

import apiParts.generators.RandomModelGenerator;
import apiParts.models.procedure.CreateProcedureRequest;
import apiParts.steps.AdminSteps;
import common.annotations.CreateProcedure;
import common.storages.SessionStorage;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.Map;
import java.util.Optional;

public class CreateProcedureExtension implements BeforeEachCallback, AfterEachCallback {

    @Override
    public void beforeEach(ExtensionContext context) {
        SessionStorage.clearProcedures();

        Optional<CreateProcedure> annotation = ExtensionUtils.findAnnotation(context, CreateProcedure.class);
        if (annotation.isEmpty()) {
            return;
        }

        int count = annotation.get().value();
        if (count < 1) {
            throw new IllegalArgumentException("@CreateProcedure value must be >= 1, but was " + count);
        }

        String patientUUID = ExtensionUtils.requirePatientUuid(CreateProcedure.class);
        for (int i = 0; i < count; i++) {
            var request = RandomModelGenerator.generate(CreateProcedureRequest.class, Map.of("patient", patientUUID));
            SessionStorage.addProcedure(request, AdminSteps.createProcedure(request));
        }
    }

    @Override
    public void afterEach(ExtensionContext context) {
        SessionStorage.clearProcedures();
    }
}
