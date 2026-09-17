package common.annotations;

import common.extensions.CreatePatientExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Before each test creates patient(s) via AdminSteps.createPatient()
 * and puts them to SessionStorage.
 * <p>
 * On class - applies to all tests of the class, on method - overrides the class value.
 * Each test and each @ParameterizedTest invocation gets new patients.
 * The extension is registered by the annotation itself, separate @ExtendWith is not needed.
 * <p>
 * Access in test or @BeforeEach: SessionStorage.getPatient() / getPatient(n) / getPatients()
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@ExtendWith(CreatePatientExtension.class)
public @interface CreatePatient {
    // how many patients to create
    int value() default 1;
}
