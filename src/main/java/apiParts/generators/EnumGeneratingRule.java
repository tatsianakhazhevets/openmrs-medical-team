package apiParts.generators;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Random constant of {@link #enumClass()}.
 * String field gets the value of {@link #valueMethod()} (uuid by default), enum field gets the constant itself.
 * Repeatable: one rule per {@link GenerationProfile}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Repeatable(EnumGeneratingRules.class)
public @interface EnumGeneratingRule {
    Class<? extends Enum<?>> enumClass();

    String valueMethod() default "getUuid";

    // names of allowed constants, empty = any constant
    String[] only() default {};

    // profiles the rule applies in, empty = any profile
    GenerationProfile[] profiles() default {};
}
