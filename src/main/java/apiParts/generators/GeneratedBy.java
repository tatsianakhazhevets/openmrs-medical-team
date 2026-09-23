package apiParts.generators;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.function.Supplier;

/**
 * Value comes from a supplier, for values no other rule can describe:
 * data from test setup (patient uuid) or domain-specific lists (vitals obs).
 * Supplier needs a no-args constructor.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface GeneratedBy {
    Class<? extends Supplier<?>> value();

    // profiles the rule applies in, empty = any profile
    GenerationProfile[] profiles() default {};
}
