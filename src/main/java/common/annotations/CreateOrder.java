package common.annotations;

import common.extensions.CreateOrderExtension;
import org.junit.jupiter.api.extension.ExtendWith;

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
 * Needs a patient: declare @CreatePatient ABOVE @CreateOrder on the same level
 * (or on class if @CreateOrder is on method) - extensions run in declaration order.
 * <p>
 * Access: SessionStorage.getOrderUuid(), getOrderEncounter().
 * Expected model: AdminSteps.drugOrderEncounterRequest / labOrderEncounterRequest(patientUUID).
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@ExtendWith(CreateOrderExtension.class)
public @interface CreateOrder {
    Type value();

    enum Type {
        DRUG,
        LAB
    }
}
