package uiParts.helpers;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.concurrent.ThreadLocalRandom;

public class AppointmentDates {

    public static LocalDate randomAppointmentDate() {
        LocalDate today = LocalDate.now();
        LocalDate lastDayOfMonth = YearMonth.from(today).atEndOfMonth();

        long daysBetween = lastDayOfMonth.toEpochDay() - today.toEpochDay();

        return today.plusDays(
                ThreadLocalRandom.current().nextLong(daysBetween + 1)
        );
    }

    public static LocalDate randomIssuedDate(LocalDate appointmentDate) {
        LocalDate today = LocalDate.now();
        LocalDate firstDayOfMonth = YearMonth.from(today).atDay(1);

        LocalDate lastAllowedDate = appointmentDate.isBefore(today)
                ? appointmentDate
                : today;

        long daysBetween = lastAllowedDate.toEpochDay()
                - firstDayOfMonth.toEpochDay();

        return firstDayOfMonth.plusDays(
                ThreadLocalRandom.current().nextLong(daysBetween + 1)
        );
    }
}