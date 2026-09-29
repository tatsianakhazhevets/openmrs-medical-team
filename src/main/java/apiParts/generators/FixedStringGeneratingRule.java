package apiParts.generators;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Field always gets the given string, e.g. {@code @FixedStringGeneratingRule(DrugOrder.SIMPLE_DOSING)}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface FixedStringGeneratingRule {
    String value();
}
