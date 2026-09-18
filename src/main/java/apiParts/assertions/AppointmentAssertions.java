package apiParts.assertions;

import apiParts.models.appointment.CreateAppointmentRequest;
import apiParts.models.appointment.CreateAppointmentResponse;
import org.assertj.core.api.SoftAssertions;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

public class AppointmentAssertions {

    private AppointmentAssertions() {
    }

    public static void assertMatchesRequest(
            SoftAssertions softly,
            CreateAppointmentResponse actual,
            CreateAppointmentRequest expected) {

        softly.assertThat(actual.getPatient().getUuid())
                .isEqualTo(expected.getPatientUuid());

        softly.assertThat(actual.getService().getUuid())
                .isEqualTo(expected.getServiceUuid());

        softly.assertThat(actual.getLocation().getUuid())
                .isEqualTo(expected.getLocationUuid());

        softly.assertThat(actual.getAppointmentKind())
                .isEqualTo(expected.getAppointmentKind());

        softly.assertThat(actual.getComments())
                .isEqualTo(expected.getComments());

        softly.assertThat(actual.getStartDateTime())
                .isEqualTo(toEpochMillis(expected.getStartDateTime()));

        softly.assertThat(actual.getEndDateTime())
                .isEqualTo(toEpochMillis(expected.getEndDateTime()));

        softly.assertThat(actual.getProviders())
                .hasSize(expected.getProviders().size());

        softly.assertThat(actual.getProviders().get(0).getUuid())
                .isEqualTo(expected.getProviders().get(0).getUuid());
    }
    public static void assertMatchesPostAndGet(
            SoftAssertions softly,
            CreateAppointmentResponse postResponse,
            CreateAppointmentResponse getResponse) {

        softly.assertThat(getResponse.getUuid())
                .isEqualTo(postResponse.getUuid());

        softly.assertThat(getResponse.getPatient().getUuid())
                .isEqualTo(postResponse.getPatient().getUuid());

        softly.assertThat(getResponse.getService().getUuid())
                .isEqualTo(postResponse.getService().getUuid());

        softly.assertThat(getResponse.getLocation().getUuid())
                .isEqualTo(postResponse.getLocation().getUuid());

        softly.assertThat(getResponse.getAppointmentKind())
                .isEqualTo(postResponse.getAppointmentKind());

        softly.assertThat(getResponse.getComments())
                .isEqualTo(postResponse.getComments());

        softly.assertThat(getResponse.getStartDateTime())
                .isEqualTo(postResponse.getStartDateTime());

        softly.assertThat(getResponse.getEndDateTime())
                .isEqualTo(postResponse.getEndDateTime());

        softly.assertThat(getResponse.getProviders())
                .hasSize(postResponse.getProviders().size());

        softly.assertThat(getResponse.getProviders().get(0).getUuid())
                .isEqualTo(postResponse.getProviders().get(0).getUuid());
    }

    private static String toEpochMillis(String dateTime) {
        return String.valueOf(
                OffsetDateTime.parse(dateTime, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                        .toInstant()
                        .toEpochMilli()
        );
    }
}