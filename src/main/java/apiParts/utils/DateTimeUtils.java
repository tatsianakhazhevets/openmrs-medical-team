package apiParts.utils;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

public class DateTimeUtils {

    // Offset with colon for request bodies: 2026-09-17T22:00:00+03:00
    public static final DateTimeFormatter OPENMRS_REQUEST_DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");

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
}
