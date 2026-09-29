package apiParts.generators;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Field gets uuid of the provider linked to admin user (AdminSteps#getCurrentProviderUuid), e.g. order.orderer.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ProviderUuidGeneratingRule {
}
