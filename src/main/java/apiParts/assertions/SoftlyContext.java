package apiParts.assertions;

import org.assertj.core.api.SoftAssertions;

/**
 * Current test's {@link SoftAssertions}, so {@link ModelAssertions} does not need it as a parameter.
 * Set by BaseTest before each test, cleared after - ThreadLocal, so parallel tests do not share it.
 */
public final class SoftlyContext {

    private static final ThreadLocal<SoftAssertions> INSTANCE = new ThreadLocal<>();

    private SoftlyContext() {
    }

    public static void set(SoftAssertions softly) {
        INSTANCE.set(softly);
    }

    public static SoftAssertions get() {
        SoftAssertions softly = INSTANCE.get();
        if (softly == null) {
            throw new IllegalStateException(
                    "No SoftAssertions for this test - call SoftlyContext.set(...) before using ModelAssertions "
                            + "(BaseTest.setUpTest() does this automatically)");
        }
        return softly;
    }

    public static void clear() {
        INSTANCE.remove();
    }
}
