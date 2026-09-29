package apiParts.generators;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Field value is taken from another (already generated) field of the same model:
 * {@code field} value, then {@code valueMethod} called on it.
 * E.g. {@code @DependsOnFieldGeneratingRule(field = "drug", valueMethod = "getConceptUuid")} - concept of the drug.
 * Generated after all other fields, so field order does not matter.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface DependsOnFieldGeneratingRule {
    String field();

    String valueMethod();
}
