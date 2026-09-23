package apiParts.generators;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Field is left null (and omitted from JSON with NON_NULL), e.g. optional visit / encounterDatetime.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface SkipGeneration {
    // profiles the rule applies in, empty = any profile
    GenerationProfile[] profiles() default {};
}
