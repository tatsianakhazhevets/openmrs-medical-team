package uiParts.helpers;

import apiParts.config.Config;
import io.qameta.allure.Allure;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Video of a Selenoid browser session attached to the Allure report.
 * <p>
 * Enabled by uiVideo=true (config.properties / -DuiVideo=true / env UIVIDEO) and works only with Selenoid (uiRemote).
 * Selenoid records the whole session and names the file {sessionId}.mp4; the file is complete only after
 * the session is closed. With a new browser per test the video shows exactly the failed test; with a reused browser
 * (uiReuseBrowser=true) it also shows previous tests of the session, the failed one is at the end.
 * <p>
 * Selenoid records every session (it does not know in advance whether the test fails):
 * - failed test: the video is attached to Allure, then deleted from Selenoid;
 * - passed test: the video is deleted from Selenoid in the background, the test does not wait for it.
 */
public final class SelenoidVideo {
    private static final Duration WAIT_FOR_FILE = Duration.ofSeconds(30);
    private static final Duration POLL_INTERVAL = Duration.ofSeconds(1);
    private static final HttpClient HTTP = HttpClient.newHttpClient();
    // deletes videos of passed tests; the JVM waits for pending deletions on exit
    private static final ExecutorService CLEANUP = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "selenoid-video-cleanup");
        thread.setDaemon(true);
        return thread;
    });

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            CLEANUP.shutdown();
            try {
                CLEANUP.awaitTermination(WAIT_FOR_FILE.toSeconds(), TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }));
    }

    private SelenoidVideo() {
    }

    public static boolean isEnabled() {
        String video = Config.getProperty("uiVideo");
        String remote = Config.getProperty("uiRemote");
        return Boolean.parseBoolean(video == null ? "false" : video.trim()) && remote != null && !remote.isBlank();
    }

    // call after the session is closed: Selenoid finishes the file a few seconds later
    public static void attachToAllure(String sessionId) {
        URI video = videoUri(sessionId);
        byte[] content = download(video);
        if (content == null) {
            Allure.step("Video of the browser session was not found: " + video);
            return;
        }
        Allure.addAttachment("Video of the failed test",
                "video/mp4", new ByteArrayInputStream(content), "mp4");
        delete(video);
    }

    // call after the session is closed: the file appears a few seconds later, so the deletion is retried until
    // Selenoid confirms it (or WAIT_FOR_FILE passes). Runs in the background.
    public static void deleteInBackground(String sessionId) {
        URI video = videoUri(sessionId);
        CLEANUP.submit(() -> {
            long deadline = System.nanoTime() + WAIT_FOR_FILE.toNanos();
            while (System.nanoTime() < deadline) {
                if (delete(video)) {
                    return;
                }
                try {
                    Thread.sleep(POLL_INTERVAL.toMillis());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        });
    }

    private static byte[] download(URI video) {
        long deadline = System.nanoTime() + WAIT_FOR_FILE.toNanos();
        HttpRequest request = HttpRequest.newBuilder(video).GET().build();
        while (System.nanoTime() < deadline) {
            try {
                HttpResponse<byte[]> response = HTTP.send(request, HttpResponse.BodyHandlers.ofByteArray());
                if (response.statusCode() == 200 && response.body().length > 0) {
                    return response.body();
                }
                Thread.sleep(POLL_INTERVAL.toMillis());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            } catch (Exception notReadyYet) {
                // Selenoid is still finishing the file - try again
            }
        }
        return null;
    }

    // true - Selenoid deleted the file; false - no file yet (404) or Selenoid is not reachable
    private static boolean delete(URI video) {
        try {
            HttpResponse<Void> response = HTTP.send(HttpRequest.newBuilder(video).DELETE().build(),
                    HttpResponse.BodyHandlers.discarding());
            return response.statusCode() / 100 == 2;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        } catch (Exception notDeleted) {
            return false;
        }
    }

    // uiRemote = http://localhost:4444/wd/hub -> http://localhost:4444/video/{sessionId}.mp4
    private static URI videoUri(String sessionId) {
        URI remote = URI.create(Config.getProperty("uiRemote").trim());
        return remote.resolve("/video/" + sessionId + ".mp4");
    }
}
