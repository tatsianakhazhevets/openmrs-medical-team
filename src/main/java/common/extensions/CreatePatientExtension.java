package common.extensions;

import apiParts.models.patient.CreatePatientResponse;
import apiParts.steps.AdminSteps;
import common.annotations.CreatePatient;
import common.storages.SessionStorage;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// BeforeEachCallback runs before @BeforeEach of the test class,
// so the patient is already in SessionStorage in setUp()
public class CreatePatientExtension implements BeforeEachCallback, AfterEachCallback {

    @Override
    public void beforeEach(ExtensionContext context) {
        SessionStorage.clearPatients();

        Optional<CreatePatient> annotation = ExtensionUtils.findAnnotation(context, CreatePatient.class);
        if (annotation.isEmpty()) {
            return;
        }

        int count = annotation.get().value();
        if (count < 1) {
            throw new IllegalArgumentException("@CreatePatient value must be >= 1, but was " + count);
        }

        List<CreatePatientResponse> patients = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            patients.add(AdminSteps.createPatient());
        }
        SessionStorage.addPatients(patients);
    }

    @Override
    public void afterEach(ExtensionContext context) {
        SessionStorage.clearPatients();
    }
}
