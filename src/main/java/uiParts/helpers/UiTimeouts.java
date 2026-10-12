package uiParts.helpers;

import apiParts.config.Config;

import java.time.Duration;

/**
 * UI wait timeouts from config.properties (uiTimeout / uiLongTimeout, ms).
 * Config reads a system property or an environment variable first, so CI can raise them without code changes.
 */
public final class UiTimeouts {
    // default wait for every Selenide check / action (Configuration.timeout)
    public static final Duration DEFAULT = Duration.ofMillis(Long.parseLong(Config.getProperty("uiTimeout").trim()));
    // parts the app loads lazily on first use: chart widgets, workspace forms, modals
    public static final Duration LONG = Duration.ofMillis(Long.parseLong(Config.getProperty("uiLongTimeout").trim()));

    private UiTimeouts() {
    }
}
