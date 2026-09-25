package common.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Before each test creates order encounter with one order for the first patient from SessionStorage
 * and puts the response to SessionStorage.
 * <p>
 * DRUG - AdminSteps.createDrugOrderEncounter (Aspirin, outpatient),
 * LAB - AdminSteps.createLabOrderEncounter (Alkaline phosphatase, inpatient ward).
 * One order only: server does not create second active drug order for the same drug.
 * <p>
 * Needs a patient: mark test class or method with @CreatePatient too (in any order and on any level).
 * Handled by CreateOrderExtension, registered in apiTests.BaseTest after CreatePatientExtension.
 * <p>
 * Access: SessionStorage.getOrderUuid(), getOrderEncounter().
 * Expected model: OrderTestData.drugOrderEncounterRequest / labOrderEncounterRequest(patientUUID, ordererUUID).
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface CreateOrder {
    Type value();

    enum Type {
        DRUG,
        LAB
    }
}
