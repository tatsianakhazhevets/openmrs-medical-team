package common.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Before each test creates valid random procedure(s) (RandomModelGenerator, rules in CreateProcedureRequest)
 * for the first patient from SessionStorage and puts the request and the response to SessionStorage.
 * <p>
 * Needs a patient: mark test class or method with @CreatePatient too (in any order and on any level).
 * Handled by CreateProcedureExtension, registered in apiTests.BaseTest after CreatePatientExtension.
 * <p>
 * Access: SessionStorage.getProcedure() / getProcedure(n) - response,
 * SessionStorage.getProcedureRequest() / getProcedureRequest(n) - sent request (expected model).
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface CreateProcedure {
    // how many procedures to create
    int value() default 1;
}
