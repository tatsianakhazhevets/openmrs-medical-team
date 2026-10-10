package apiTests.appointment;

import apiParts.assertions.ModelAssertions;
import apiParts.generators.RandomModelGenerator;
import apiParts.models.appointment.*;
import apiParts.skelethon.endpoints.Endpoint;
import apiParts.skelethon.requests.crud.CrudRequester;
import apiParts.specs.RequestSpecs;
import apiParts.specs.ResponseSpecs;
import apiParts.steps.AppointmentSteps;
import apiParts.utils.DateTimeUtils;
import apiTests.BaseTest;
import common.annotations.CreatePatient;
import common.storages.SessionStorage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

@CreatePatient
public class AppointmentTests extends BaseTest {
    private String patientUUID;
    private String appointmentUUID;

    @BeforeEach
    void setUp() {
        patientUUID = SessionStorage.getPatient().getUuid();
    }

    @AfterEach
    void tearDown() {
        if (appointmentUUID != null) {
            AppointmentSteps.cancelAppointment(appointmentUUID);
        }
    }

    @Test
    void shouldCreateAppointment() {
        CreateAppointmentRequest request =
                RandomModelGenerator.generate(CreateAppointmentRequest.class);

        CreateAppointmentResponse appointment =
                AppointmentSteps.createAppointment(request);

        appointmentUUID = appointment.getUuid();

        CreateAppointmentResponse foundAppointment =
                AppointmentSteps.getAppointmentByUuid(appointmentUUID, request.getStartDateTime());

        ModelAssertions.assertThatModels(request, foundAppointment)
                .as("appointment after GET").match();

        softly.assertThat(foundAppointment.getUuid()).isNotNull();
        softly.assertThat(foundAppointment.getAppointmentNumber()).isNotNull();
        softly.assertThat(foundAppointment.getStatus()).isEqualTo(AppointmentStatus.SCHEDULED.getValue());
        softly.assertThat(foundAppointment.getVoided()).isFalse();
        softly.assertThat(foundAppointment.getRecurring()).isFalse();
    }

    @Test
    void shouldSearchAppointmentsByPatient() {
        CreateAppointmentRequest request =
                RandomModelGenerator.generate(CreateAppointmentRequest.class);

        CreateAppointmentResponse appointment =
                AppointmentSteps.createAppointment(request);
        appointmentUUID = appointment.getUuid();

        CreateAppointmentResponse foundAppointment =
                AppointmentSteps.getAppointmentByUuid(appointmentUUID, request.getStartDateTime());

        ModelAssertions.assertThatModels(request, foundAppointment)
                .as("appointment after GET").match();

        List<CreateAppointmentResponse> appointments =
                AppointmentSteps.searchAppointments(patientUUID);

        CreateAppointmentResponse foundInSearch =
                AppointmentSteps.requireAppointment(appointments, appointmentUUID);

        ModelAssertions.assertThatModels(request, foundInSearch)
                .as("appointment after search").match();
    }

    @Test
    void shouldUpdateAppointment() {
        CreateAppointmentResponse appointment =
                AppointmentSteps.createAppointment();
        appointmentUUID = appointment.getUuid();

        UpdateAppointmentRequest request =
                RandomModelGenerator.generate(UpdateAppointmentRequest.class);
        request.setUuid(appointmentUUID);

        AppointmentSteps.updateAppointment(request);

        CreateAppointmentResponse updatedAppointment =
                AppointmentSteps.getAppointmentByUuid(appointmentUUID, request.getStartDateTime());

        ModelAssertions.assertThatModels(request, updatedAppointment)
                .as("appointment after GET")
                .match();

        softly.assertThat(updatedAppointment.getUuid()).isEqualTo(appointmentUUID);
        softly.assertThat(updatedAppointment.getStatus()).isEqualTo(AppointmentStatus.CHECKED_IN.getValue());
        softly.assertThat(updatedAppointment.getProviders()).anyMatch(provider ->
                provider.getUuid().equals(request.getProviders().get(0).getUuid()) &&
                        AppointmentProviderResponse.ACCEPTED.getValue().equals(provider.getResponse()));
    }

    @Test
    void shouldCancelAppointment() {
        CreateAppointmentResponse appointment =
                AppointmentSteps.createAppointment();
        appointmentUUID = appointment.getUuid();

        AppointmentSteps.cancelAppointment(appointmentUUID);
        appointmentUUID = null;

        CreateAppointmentResponse cancelledAppointment =
                AppointmentSteps.getAppointmentByUuid(
                        appointment.getUuid(),
                        appointment.getStartDateTime()
                );

        softly.assertThat(cancelledAppointment.getUuid()).isEqualTo(appointment.getUuid());
        softly.assertThat(cancelledAppointment.getStatus()).isEqualTo(AppointmentStatus.CANCELLED.getValue());
    }

    @Test
    void shouldNotCreateAppointmentWithoutPatient() {
        CreateAppointmentRequest request =
                RandomModelGenerator.generate(CreateAppointmentRequest.class);
        request.setPatientUuid(null);

        new CrudRequester(
                RequestSpecs.adminSpec(),
                Endpoint.APPOINTMENT_POST,
                ResponseSpecs.requestReturnsAppointmentCannotBeCreatedWithoutPatient()).create(request);
        List<CreateAppointmentResponse> appointmentsAfter =
                AppointmentSteps.getAppointments(
                        DateTimeUtils.toForDate(request.getStartDateTime()));

        softly.assertThat(appointmentsAfter)
                .noneMatch(appointment ->
                        request.getComments().equals(appointment.getComments()));
    }

    @Test
    void shouldNotCreateAppointmentWithoutService() {
        CreateAppointmentRequest request =
                RandomModelGenerator.generate(CreateAppointmentRequest.class);
        request.setServiceUuid(null);

        new CrudRequester(
                RequestSpecs.adminSpec(),
                Endpoint.APPOINTMENT_POST,
                ResponseSpecs.requestReturnsAppointmentCannotBeCreatedWithoutService()
        ).create(request);

        List<CreateAppointmentResponse> appointmentsAfter =
                AppointmentSteps.getAppointments(
                        DateTimeUtils.toForDate(request.getStartDateTime()));

        softly.assertThat(appointmentsAfter)
                .noneMatch(appointment ->
                        request.getPatientUuid().equals(appointment.getPatient().getUuid())
                                && request.getComments().equals(appointment.getComments()));
    }

    @Test
    void shouldNotUpdateNonExistingAppointment() {
        String nonExistingAppointmentUuid =
                RandomModelGenerator.randomUnknownUuid();

        UpdateAppointmentRequest request =
                RandomModelGenerator.generate(UpdateAppointmentRequest.class);
        request.setUuid(nonExistingAppointmentUuid);

        new CrudRequester(
                RequestSpecs.adminSpec(),
                Endpoint.APPOINTMENT_POST,
                ResponseSpecs.requestReturnsBadRequest()
        ).create(request);

        List<CreateAppointmentResponse> appointments = AppointmentSteps.getAppointments(
                DateTimeUtils.toForDate(request.getStartDateTime()));

        softly.assertThat(appointments).noneMatch(appointment ->
                nonExistingAppointmentUuid.equals(appointment.getUuid()));
    }
}
