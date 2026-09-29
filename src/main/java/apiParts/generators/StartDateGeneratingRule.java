package apiParts.generators;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Start of a date range (e.g. startDateTime of a procedure).
 * <p>
 * Start and end are generated as a pair and shared inside one model instance:
 * start - random moment in the past, end - start + random time up to 10 hours.
 * Pair is created by the first annotated field, so field order does not matter.
 * Format: {@code yyyy-MM-dd'T'HH:mm:ssXXX} (DateTimeUtils#OPENMRS_REQUEST_DATE_TIME).
 *
 * @see EndDateGeneratingRule
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface StartDateGeneratingRule {
}
