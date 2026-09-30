package apiParts.utils;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

public class DateTimeUtils {

    // Offset dates are built in, same as the client timezone
    public static final ZoneOffset MOSCOW = ZoneOffset.ofHours(3);

    // Offset with colon for request bodies: 2026-09-17T22:00:00+03:00
    public static final DateTimeFormatter OPENMRS_REQUEST_DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");

    // Format OpenMRS writes dates in: 2026-09-17T19:00:00.000+0000 (offset without colon)
    public static final DateTimeFormatter OPENMRS_RESPONSE_DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    // Same precision, but with literal Z instead of an offset: 2026-09-17T19:00:00.000Z
    public static final DateTimeFormatter UTC_DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    // Bounds of random past / future dates: any date inside them is handled by the same server logic
    private static final int MIN_DAYS_FROM_NOW = 1;
    private static final int MAX_DAYS_FROM_NOW = 30;
    private static final int MINUTES_PER_DAY = 24 * 60;

    private DateTimeUtils() {
    }

    // Random minute 1..30 days ago, e.g. start of a procedure that already happened
    public static OffsetDateTime randomPastDateTime() {
        return now().minusDays(randomDays()).minusMinutes(randomMinuteOfDay());
    }

    // Random minute 1..30 days ahead, e.g. start of a procedure that has not happened yet
    public static OffsetDateTime randomFutureDateTime() {
        return now().plusDays(randomDays()).plusMinutes(randomMinuteOfDay());
    }

    // OffsetDateTime -> request body format: 2026-09-17T22:00:00+03:00
    public static String toRequestString(OffsetDateTime dateTime) {
        return dateTime.format(OPENMRS_REQUEST_DATE_TIME);
    }

    // "2026-09-17T22:00:00+03:00" and "2026-09-17T19:00:00.000+0000" -> "2026-09-17T19:00:00Z"
    public static String toInstantString(String dateTime) {
        if (dateTime == null) {
            return null;
        }
        // OpenMRS writes offset without colon (+0000), ISO parser expects +00:00
        String iso = dateTime.replaceFirst("([+-]\\d{2})(\\d{2})$", "$1:$2");
        return OffsetDateTime.parse(iso).toInstant().toString();
    }


    public static OffsetDateTime now() {
        return OffsetDateTime.now(MOSCOW)
                .withSecond(0)
                .withNano(0);
    }

    public static OffsetDateTime nowPlusMinutes(long minutes) {
        return now().plusMinutes(minutes);
    }

    public static OffsetDateTime nowPlusDays(long days) {
        return now().plusDays(days);
    }

    public static OffsetDateTime nowMinusMonths(long months) {
        return now().minusMonths(months);
    }

    private static int randomDays() {
        return ThreadLocalRandom.current().nextInt(MIN_DAYS_FROM_NOW, MAX_DAYS_FROM_NOW + 1);
    }

    private static int randomMinuteOfDay() {
        return ThreadLocalRandom.current().nextInt(MINUTES_PER_DAY);
    }
}
