package apiParts.generators;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ThreadLocalRandom;

public class RandomDataGenerator {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    public static String generateBirthDate() {
        LocalDate start = LocalDate.of(1950, 1, 1);
        LocalDate end = LocalDate.of(2025, 12, 31);

        long daysBetween = ChronoUnit.DAYS.between(start, end);

        LocalDate randomDate = start.plusDays(
                ThreadLocalRandom.current().nextLong(daysBetween + 1)
        );

        return randomDate.format(FORMATTER);
    }
}