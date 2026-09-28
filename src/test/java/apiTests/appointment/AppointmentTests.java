package apiTests.appointment;

import apiParts.assertions.ModelAssertions;
import apiParts.generators.RandomModelGenerator;
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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.List;

@CreatePatient
public class AppointmentTests extends BaseTest {
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

    @Test
    void shouldCreateAppointment() {
        CreateAppointmentRequest request =
                RandomModelGenerator.generate(CreateAppointmentRequest.class);

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
        CreateAppointmentRequest request =
                RandomModelGenerator.generate(CreateAppointmentRequest.class);

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
        CreateAppointmentResponse appointment =
                AdminSteps.createAppointment();
        appointmentUUID = appointment.getUuid();

        UpdateAppointmentRequest request =
                RandomModelGenerator.generate(UpdateAppointmentRequest.class);
        request.setUuid(appointmentUUID);

        CreateAppointmentResponse updatedAppointment =
                AdminSteps.updateAppointment(request);

        softly.assertThat(updatedAppointment.getUuid()).isEqualTo(appointmentUUID);
        softly.assertThat(updatedAppointment.getStatus()).isEqualTo(AppointmentStatus.CHECKED_IN.getValue());
        softly.assertThat(updatedAppointment.getComments()).isEqualTo(request.getComments());
        softly.assertThat(updatedAppointment.getProviders()).anyMatch(provider ->
                provider.getUuid().equals(request.getProviders().get(0).getUuid()) &&
                        AppointmentProviderResponse.ACCEPTED.getValue().equals(provider.getResponse()));
        softly.assertThat(updatedAppointment.getStartDateTime()).isEqualTo(String.valueOf(
                OffsetDateTime.parse(
                                request.getStartDateTime(),
                                DateTimeUtils.OPENMRS_RESPONSE_DATE_TIME)
                        .toInstant().toEpochMilli()));
        softly.assertThat(updatedAppointment.getEndDateTime()).isEqualTo(String.valueOf(
                OffsetDateTime.parse(
                        request.getEndDateTime(),
                        DateTimeUtils.OPENMRS_RESPONSE_DATE_TIME).toInstant().toEpochMilli()));
        softly.assertAll();
    }

    @Test
    void shouldCancelAppointment() {
        CreateAppointmentResponse appointment =
                AdminSteps.createAppointment();

        CreateAppointmentResponse cancelledAppointment =
                AdminSteps.cancelAppointment(appointment.getUuid());

        softly.assertThat(cancelledAppointment.getUuid()).isEqualTo(appointment.getUuid());
        softly.assertThat(cancelledAppointment.getStatus()).isEqualTo(AppointmentStatus.CANCELLED.getValue());
        softly.assertAll();
    }

    @Test
    void shouldNotCreateAppointmentWithoutPatient() {
        CreateAppointmentRequest request =
                RandomModelGenerator.generate(CreateAppointmentRequest.class);
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
        CreateAppointmentRequest request =
                RandomModelGenerator.generate(CreateAppointmentRequest.class);
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
        UpdateAppointmentRequest request =
                RandomModelGenerator.generate(UpdateAppointmentRequest.class);
        request.setUuid(NON_EXISTING_APPOINTMENT_UUID);

        new CrudRequester(
                RequestSpecs.adminSpec(),
                Endpoint.APPOINTMENT_POST,
                ResponseSpecs.requestReturnsBadRequest()
        ).create(request);
    }
}
