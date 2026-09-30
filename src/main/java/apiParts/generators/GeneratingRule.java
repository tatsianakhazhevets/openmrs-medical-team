package apiParts.generators;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.RECORD_COMPONENT, ElementType.PARAMETER})
public @interface GeneratingRule {
    String regex() default "";

    String property() default "";

    boolean nullable() default false;

    BooleanGeneration booleanValue() default BooleanGeneration.RANDOM;

    GenerationStrategy strategy() default GenerationStrategy.RANDOM;

    String fixedValue() default "";

    Class<? extends Enum<?>> enumClass() default EmptyEnum.class;

    String enumValue() default "";

    String[] excludedEnumValues() default {};

    String valueMethod() default "getUuid";

    double min() default 0;

    double max() default 0;

    int scale() default 0;

    int minSize() default 1;

    int maxSize() default 3;

    int minYear() default 1900;

    int maxYear() default 2025;

    int minutesFromNow() default 0;

    String baseField() default "";

    int minutesFromBase() default 0;

    String sourceField() default "";

    int patientNumber() default 1;

    enum EmptyEnum {
        VALUE
    }
}
