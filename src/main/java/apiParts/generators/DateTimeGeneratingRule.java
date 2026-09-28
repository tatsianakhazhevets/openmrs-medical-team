package apiParts.generators;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface DateTimeGeneratingRule {

    int minutesFromNow() default 0;

    String baseField() default "";

    int minutesFromBase() default 0;
}