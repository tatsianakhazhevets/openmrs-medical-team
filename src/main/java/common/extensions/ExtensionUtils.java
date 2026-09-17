package common.extensions;

import common.storages.SessionStorage;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.support.AnnotationSupport;

import java.lang.annotation.Annotation;
import java.util.Optional;

final class ExtensionUtils {

    private ExtensionUtils() {
    }

    // annotation on method takes precedence over annotation on class (and its superclasses)
    static <A extends Annotation> Optional<A> findAnnotation(ExtensionContext context, Class<A> annotationType) {
        return AnnotationSupport.findAnnotation(context.getRequiredTestMethod(), annotationType)
                .or(() -> AnnotationSupport.findAnnotation(context.getRequiredTestClass(), annotationType));
    }

    // uuid of the first patient for preconditions that need a patient
    static String requirePatientUuid(Class<? extends Annotation> annotationType) {
        if (SessionStorage.getPatients().isEmpty()) {
            throw new IllegalStateException("@" + annotationType.getSimpleName()
                    + " needs a patient - declare @CreatePatient above @" + annotationType.getSimpleName());
        }
        return SessionStorage.getPatient().getUuid();
    }
}
