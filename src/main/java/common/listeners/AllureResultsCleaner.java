package common.listeners;

import org.junit.platform.launcher.LauncherSession;
import org.junit.platform.launcher.LauncherSessionListener;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Properties;
import java.util.stream.Stream;

/**
 * Deletes Allure results of previous runs once at the start of each test run -
 * the same for Run in IDE and for mvn test (both go through JUnit Platform launcher).
 * <p>
 * Directory: system property / allure.properties "allure.results.directory" (target/allure-results),
 * the same one Allure writes to. Registered in META-INF/services/org.junit.platform.launcher.LauncherSessionListener.
 * <p>
 * Note: running a single test also removes results of the previous full run.
 */
public class AllureResultsCleaner implements LauncherSessionListener {
    private static final String ALLURE_PROPERTIES = "allure.properties";
    private static final String RESULTS_DIRECTORY_KEY = "allure.results.directory";
    private static final String DEFAULT_RESULTS_DIRECTORY = "allure-results";   // Allure default

    @Override
    public void launcherSessionOpened(LauncherSession session) {
        Path resultsDirectory = Path.of(resultsDirectory());
        if (!Files.isDirectory(resultsDirectory)) {
            return;
        }
        // deepest paths first, so directories are empty when deleted; the results directory itself is kept
        try (Stream<Path> paths = Files.walk(resultsDirectory)) {
            paths.sorted(Comparator.reverseOrder())
                    .filter(path -> !path.equals(resultsDirectory))
                    .forEach(path -> path.toFile().delete());
        } catch (IOException e) {
            // tests must not fail because of the report: old results just stay in the report
            System.err.println("[AllureResultsCleaner] failed to clean " + resultsDirectory.toAbsolutePath() + ": " + e);
        }
    }

    private static String resultsDirectory() {
        String fromSystemProperty = System.getProperty(RESULTS_DIRECTORY_KEY);
        if (fromSystemProperty != null) {
            return fromSystemProperty;
        }
        Properties allureProperties = new Properties();
        try (InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(ALLURE_PROPERTIES)) {
            if (in != null) {
                allureProperties.load(in);
            }
        } catch (IOException ignored) {
            // no allure.properties - Allure uses its default directory too
        }
        return allureProperties.getProperty(RESULTS_DIRECTORY_KEY, DEFAULT_RESULTS_DIRECTORY);
    }
}
