package apiParts.utils;

public class RepeatUtil {
    public static void repeat(int times, Runnable action) {
        for(int i = 0; i < times; i++) {
            action.run();
        }
    }
}