package apiTests.appointment;

import apiParts.assertions.ModelAssertions;
import apiParts.generators.RandomModelGenerator;
import apiParts.models.Location;
import apiParts.models.appointment.*;
import apiParts.skelethon.endpoints.Endpoint;
import apiParts.skelethon.requests.crud.CrudRequester;
import apiParts.specs.RequestSpecs;
import apiParts.specs.ResponseSpecs;
import apiParts.steps.AdminSteps;
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

    private CreateAppointmentRequest appointmentRequest() {
        CreateAppointmentRequest request =
                RandomModelGenerator.generate(CreateAppointmentRequest.class);

        OffsetDateTime startDateTime = DateTimeUtils.nowPlusMinutes(60);
        OffsetDateTime endDateTime = startDateTime.plusMinutes(30);

        request.setUuid(null);
        request.setPatientUuid(patientUUID);
        request.setAppointmentKind(AppointmentKind.SCHEDULED.getValue());
        request.setStatus(AppointmentStatus.SCHEDULED.getValue());
        request.setServiceUuid(AppointmentService.GENERAL_MEDICINE.getUuid());
        request.setLocationUuid(Location.OUTPATIENT_CLINIC.getUuid());
        request.setProviders(List.of(
                new CreateAppointmentRequest.Provider(
                        AppointmentProvider.JAKE_DOCTOR.getUuid()
                )
        ));
        request.setStartDateTime(
                startDateTime.format(DateTimeUtils.OPENMRS_REQUEST_DATE_TIME)
        );
        request.setEndDateTime(
                endDateTime.format(DateTimeUtils.OPENMRS_REQUEST_DATE_TIME)
        );
        request.setDateAppointmentScheduled(
                DateTimeUtils.now()
                        .format(DateTimeUtils.OPENMRS_REQUEST_DATE_TIME)
        );

        return request;
    }

    @Test
    void shouldCreateAppointment() {
        CreateAppointmentRequest request = appointmentRequest();

        CreateAppointmentResponse appointment =
                AdminSteps.createAppointment(request);
        appointmentUUID = appointment.getUuid();

        ModelAssertions.assertThatModels(softly, request, appointment).as("POST /appointment response").match();
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
        CreateAppointmentRequest request = appointmentRequest();

        CreateAppointmentResponse appointment =
                AdminSteps.createAppointment(request);
        appointmentUUID = appointment.getUuid();

        List<CreateAppointmentResponse> appointments =
                AdminSteps.searchAppointments(patientUUID);

        CreateAppointmentResponse foundAppointment = appointments.stream()
                .filter(found -> found.getUuid().equals(appointment.getUuid()))
                .findFirst()
                .orElseThrow(() -> new AssertionError(
                        "Created appointment was not found in search results"
                ));

        ModelAssertions.assertThatModels(softly, request, foundAppointment).as("appointment after GET").match();
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

        CreateAppointmentRequest request =
                RandomModelGenerator.generate(CreateAppointmentRequest.class);
        request.setUuid(appointmentUUID);
        request.setAppointmentKind(AppointmentKind.SCHEDULED.getValue());
        request.setStatus(AppointmentStatus.CHECKED_IN.getValue());
        request.setServiceUuid(appointment.getService().getUuid());
        request.setStartDateTime(startDateTime.format(DateTimeUtils.OPENMRS_REQUEST_DATE_TIME));
        request.setEndDateTime(endDateTime.format(DateTimeUtils.OPENMRS_REQUEST_DATE_TIME));
        request.setLocationUuid(appointment.getLocation().getUuid());
        request.setProviders(List.of(new CreateAppointmentRequest.Provider(
                AppointmentProvider.JAKE_DOCTOR.getUuid())));
        request.setPatientUuid(appointment.getPatient().getUuid());
        request.setComments(randomUpdatedComment);
        request.setDateAppointmentScheduled(scheduledAt);

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
        CreateAppointmentRequest request = appointmentRequest();
        request.setPatientUuid(null);

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
        CreateAppointmentRequest request = appointmentRequest();
        request.setServiceUuid(null);

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
        CreateAppointmentRequest request = appointmentRequest();
        request.setUuid(NON_EXISTING_APPOINTMENT_UUID);
        request.setStatus(AppointmentStatus.CHECKED_IN.getValue());
        request.setStartDateTime(DateTimeUtils.nowPlusMinutes(60)
                .format(DateTimeUtils.OPENMRS_REQUEST_DATE_TIME)
        );
        request.setEndDateTime(DateTimeUtils.nowPlusMinutes(90)
                .format(DateTimeUtils.OPENMRS_REQUEST_DATE_TIME)
        );
        request.setDateAppointmentScheduled(DateTimeUtils.now()
                .format(DateTimeUtils.OPENMRS_REQUEST_DATE_TIME)
        );
        request.setComments(FAKER.text().text());

        new CrudRequester(
                RequestSpecs.adminSpec(),
                Endpoint.APPOINTMENT_POST,
                ResponseSpecs.requestReturnsBadRequest()
        ).create(request);
    }
}
