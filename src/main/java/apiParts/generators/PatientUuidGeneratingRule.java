package apiParts.generators;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Field gets uuid of the patient created by @CreatePatient (taken from SessionStorage),
 * instead of a random value.
 * {@code number} - which patient to take (starts from 1), as in SessionStorage.getPatient(number).
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface PatientUuidGeneratingRule {
    int number() default 1;
}
