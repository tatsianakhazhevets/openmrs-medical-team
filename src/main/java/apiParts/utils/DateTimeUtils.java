package apiParts.utils;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class DateTimeUtils {

    // Offset dates are built in, same as the client timezone
    public static final ZoneOffset MOSCOW = ZoneOffset.ofHours(3);

    // Offset with colon for request bodies: 2026-09-17T22:00:00+03:00
    public static final DateTimeFormatter OPENMRS_REQUEST_DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");

    // Format OpenMRS writes dates in: 2026-09-17T19:00:00.000+0000 (offset without colon)
    public static final DateTimeFormatter OPENMRS_RESPONSE_DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    // Same precision, but with literal Z instead of an offset: 2026-09-17T19:00:00.000Z
    public static final DateTimeFormatter UTC_DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    private DateTimeUtils() {
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

    public static OffsetDateTime nowPlusMinutes(long minutes) {
        return OffsetDateTime.now()
                .plusMinutes(minutes)
                .withSecond(0)
                .withNano(0);
    }

    public static OffsetDateTime nowPlusDays(long days) {
        return OffsetDateTime.now()
                .plusDays(days)
                .withSecond(0)
                .withNano(0);
    }

    public static OffsetDateTime now() {
        return OffsetDateTime.now()
                .withSecond(0)
                .withNano(0);
    }
    public static OffsetDateTime nowMinusMonths(long months) {
        return now().minusMonths(months);
    }
}
