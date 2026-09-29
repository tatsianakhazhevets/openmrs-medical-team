package apiParts.generators;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * End of a date range (e.g. endDateTime of a procedure): start + random time up to 10 hours.
 *
 * @see StartDateGeneratingRule
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface EndDateGeneratingRule {
}
