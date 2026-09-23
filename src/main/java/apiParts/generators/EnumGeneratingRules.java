package apiParts.generators;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// Container for repeated @EnumGeneratingRule
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface EnumGeneratingRules {
    EnumGeneratingRule[] value();
}
