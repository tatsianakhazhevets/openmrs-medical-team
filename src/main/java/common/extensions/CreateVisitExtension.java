package common.extensions;

import apiParts.models.visit.CreateVisitResponse;
import apiParts.steps.AdminSteps;
import common.annotations.CreateVisit;
import common.storages.SessionStorage;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.Optional;

public class CreateVisitExtension implements BeforeEachCallback, AfterEachCallback {

    @Override
    public void beforeEach(ExtensionContext context) {
        SessionStorage.clearVisits();

        Optional<CreateVisit> annotation =
                ExtensionUtils.findAnnotation(context, CreateVisit.class);

        if (annotation.isEmpty()) {
            return;
        }

        String patientUUID = ExtensionUtils.requirePatientUuid(CreateVisit.class);

        CreateVisitResponse visit =
                AdminSteps.createVisitWithRequiredFields(patientUUID);

        SessionStorage.addVisit(visit);
    }

    @Override
    public void afterEach(ExtensionContext context) {
        SessionStorage.clearVisits();
    }
}