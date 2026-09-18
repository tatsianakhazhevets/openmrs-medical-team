package apiTests.appointment;

import apiParts.assertions.AppointmentAssertions;
import apiParts.models.Location;
import apiParts.models.appointment.*;
import apiParts.skelethon.endpoints.Endpoint;
import apiParts.skelethon.requests.common.CrudRequester;
import apiParts.specs.RequestSpecs;
import apiParts.specs.ResponseSpecs;
import apiParts.steps.AdminSteps;
import apiParts.testdata.AppointmentTestData;
import apiParts.utils.DateTimeUtils;
import apiTests.BaseTest;
import common.annotations.CreatePatient;
import common.storages.SessionStorage;
import net.datafaker.Faker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Locale;

@CreatePatient
public class AppointmentTests extends BaseTest {
    private static final Faker FAKER = new Faker(new Locale("en", "US"));
    private static final String NON_EXISTING_APPOINTMENT_UUID =
            "00000000-0000-0000-0000-000000000000";
    private String patientUUID;
    private String appointmentUUID;

    @BeforeEach
    void setUp() {
        patientUUID = SessionStorage.getPatient().getUuid();
    }

    @AfterEach
    void tearDown() {
        if (appointmentUUID != null) {
            AdminSteps.cancelAppointment(appointmentUUID);
        }
    }

    private CreateAppointmentRequest.CreateAppointmentRequestBuilder appointmentRequest() {
        return AppointmentTestData.appointmentRequestBuilder(patientUUID);
    }

    @Test
    void shouldCreateAppointment() {
        CreateAppointmentRequest request = appointmentRequest().build();

        CreateAppointmentResponse appointment =
                AdminSteps.createAppointment(request);
        appointmentUUID = appointment.getUuid();

        AppointmentAssertions.assertMatchesRequest(softly, appointment, request);
        softly.assertThat(appointment.getUuid()).isNotNull();
        softly.assertThat(appointment.getAppointmentNumber()).isNotNull();
        softly.assertThat(appointment.getStatus())
                .isEqualTo(AppointmentStatus.SCHEDULED.getValue());
        softly.assertThat(appointment.getVoided()).isFalse();
        softly.assertThat(appointment.getRecurring()).isFalse();

        softly.assertAll();
    }

    @Test
    void shouldSearchAppointmentsByPatient() {
        CreateAppointmentResponse appointment =
                AdminSteps.createAppointment(patientUUID);
        appointmentUUID = appointment.getUuid();

        List<CreateAppointmentResponse> appointments =
                AdminSteps.searchAppointments(patientUUID);

        CreateAppointmentResponse foundAppointment = appointments.stream()
                .filter(found -> found.getUuid().equals(appointment.getUuid()))
                .findFirst()
                .orElseThrow(() -> new AssertionError(
                        "Created appointment was not found in search results"
                ));

        AppointmentAssertions.assertMatchesPostAndGet(softly, appointment, foundAppointment);
        softly.assertAll();
    }

    @Test
    void shouldUpdateAppointment() {
        String randomUpdatedComment = FAKER.text().text();
        CreateAppointmentResponse appointment =
                AdminSteps.createAppointment(patientUUID);
        appointmentUUID = appointment.getUuid();

        OffsetDateTime startDateTime = DateTimeUtils.nowPlusMinutes(60);
        OffsetDateTime endDateTime = startDateTime.plusMinutes(30);

        String scheduledAt = DateTimeUtils.nowPlusDays(1)
                .format(DateTimeUtils.OPENMRS_REQUEST_DATE_TIME);

        CreateAppointmentRequest request = CreateAppointmentRequest.builder()
                .uuid(appointmentUUID)
                .appointmentKind(AppointmentKind.SCHEDULED.getValue())
                .status(AppointmentStatus.CHECKED_IN.getValue())
                .serviceUuid(appointment.getService().getUuid())
                .startDateTime(startDateTime.format(DateTimeUtils.OPENMRS_REQUEST_DATE_TIME))
                .endDateTime(endDateTime.format(DateTimeUtils.OPENMRS_REQUEST_DATE_TIME))
                .locationUuid(appointment.getLocation().getUuid())
                .providers(List.of(
                        CreateAppointmentRequest.Provider.builder()
                                .uuid(AppointmentProvider.JAKE_DOCTOR.getUuid())
                                .build()
                ))
                .patientUuid(appointment.getPatient().getUuid())
                .comments(randomUpdatedComment)
                .dateAppointmentScheduled(scheduledAt)
                .build();

        CreateAppointmentResponse updatedAppointment =
                AdminSteps.updateAppointment(request);

        softly.assertThat(updatedAppointment.getUuid()).isEqualTo(appointmentUUID);
        softly.assertThat(updatedAppointment.getStatus()).isEqualTo(AppointmentStatus.CHECKED_IN.getValue());
        softly.assertThat(updatedAppointment.getComments()).isEqualTo(randomUpdatedComment);
        softly.assertThat(updatedAppointment.getStartDateTime()).isEqualTo(String.valueOf(startDateTime.toInstant().toEpochMilli()));
        softly.assertThat(updatedAppointment.getEndDateTime()).isEqualTo(String.valueOf(endDateTime.toInstant().toEpochMilli()));
        softly.assertThat(updatedAppointment.getProviders()).anyMatch(provider ->
                provider.getUuid().equals(AppointmentProvider.JAKE_DOCTOR.getUuid())
                        && AppointmentProviderResponse.ACCEPTED.getValue().equals(provider.getResponse()));

        softly.assertAll();
    }

    @Test
    void shouldCancelAppointment() {
        CreateAppointmentResponse appointment =
                AdminSteps.createAppointment(patientUUID);

        CreateAppointmentResponse cancelledAppointment =
                AdminSteps.cancelAppointment(appointment.getUuid());

        softly.assertThat(cancelledAppointment.getUuid()).isEqualTo(appointment.getUuid());
        softly.assertThat(cancelledAppointment.getStatus()).isEqualTo(AppointmentStatus.CANCELLED.getValue());
        softly.assertAll();
    }

    @Test
    void shouldNotCreateAppointmentWithoutPatient() {
        CreateAppointmentRequest request = appointmentRequest()
                .patientUuid(null)
                .build();

        new CrudRequester(
                RequestSpecs.adminSpec(),
                Endpoint.APPOINTMENT_POST,
                ResponseSpecs.requestReturnsBadRequestWithMessage(
                        "Appointment cannot be created without Patient"
                )
        ).create(request);
    }

    @Test
    void shouldNotCreateAppointmentWithoutService() {
        CreateAppointmentRequest request = appointmentRequest()
                .serviceUuid(null)
                .build();

        new CrudRequester(
                RequestSpecs.adminSpec(),
                Endpoint.APPOINTMENT_POST,
                ResponseSpecs.requestReturnsBadRequestWithMessage(
                        "Appointment cannot be created without Service"
                )
        ).create(request);
    }

    @Test
    void shouldNotUpdateNonExistingAppointment() {
        OffsetDateTime startDateTime = DateTimeUtils.nowPlusMinutes(60);
        OffsetDateTime endDateTime = startDateTime.plusMinutes(30);
        CreateAppointmentRequest request = CreateAppointmentRequest.builder()
                .uuid(NON_EXISTING_APPOINTMENT_UUID)
                .appointmentKind(AppointmentKind.SCHEDULED.getValue())
                .status(AppointmentStatus.CHECKED_IN.getValue())
                .serviceUuid(AppointmentService.GENERAL_MEDICINE.getUuid())
                .startDateTime(startDateTime.format(DateTimeUtils.OPENMRS_REQUEST_DATE_TIME))
                .endDateTime(endDateTime.format(DateTimeUtils.OPENMRS_REQUEST_DATE_TIME))
                .locationUuid(Location.OUTPATIENT_CLINIC.getUuid())
                .providers(List.of(
                        CreateAppointmentRequest.Provider.builder()
                                .uuid(AppointmentProvider.JAKE_DOCTOR.getUuid())
                                .build()
                ))
                .patientUuid(patientUUID)
                .comments(FAKER.text().text())
                .dateAppointmentScheduled(
                        DateTimeUtils.now()
                                .format(DateTimeUtils.OPENMRS_REQUEST_DATE_TIME)
                )
                .build();

        new CrudRequester(
                RequestSpecs.adminSpec(),
                Endpoint.APPOINTMENT_POST,
                ResponseSpecs.requestReturnsBadRequest()
        ).create(request);
    }
}