package apiParts.generators.suppliers;

import common.storages.SessionStorage;

import java.util.function.Supplier;

/**
 * Uuid of the first patient created by @CreatePatient.
 * For another patient pass it in overrides: Map.of("patient", uuid).
 */
public class CurrentPatientUuid implements Supplier<String> {
    @Override
    public String get() {
        return SessionStorage.getPatient().getUuid();
    }
}
