package apiParts.generators;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * List of vitals obs: one obs per VitalsConcept, value is random inside the concept range
 * (see VitalsConcept#low / #high), TEXT concepts get a random sentence.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface VitalsObsGeneratingRule {
}
