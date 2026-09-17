package common.annotations;

import common.extensions.CreateProcedureExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Before each test creates valid procedure(s) via AdminSteps.createProcedure()
 * for the first patient from SessionStorage and puts the response to SessionStorage.
 * <p>
 * Needs a patient: declare @CreatePatient ABOVE @CreateProcedure on the same level
 * (or on class if @CreateProcedure is on method) - extensions run in declaration order.
 * <p>
 * Access: SessionStorage.getProcedure() / getProcedure(n).
 * Expected model: AdminSteps.procedureRequest(patientUUID) - the same request that was sent on create.
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@ExtendWith(CreateProcedureExtension.class)
public @interface CreateProcedure {
    // how many procedures to create
    int value() default 1;
}
