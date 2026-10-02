package apiParts.steps;

import apiParts.generators.RandomModelGenerator;
import apiParts.models.appointment.AppointmentStatusChangeRequest;
import apiParts.models.appointment.CreateAppointmentRequest;
import apiParts.models.appointment.CreateAppointmentResponse;
import apiParts.models.appointment.UpdateAppointmentRequest;
import apiParts.models.search.SearchParams;
import apiParts.skelethon.endpoints.Endpoint;
import apiParts.skelethon.requests.action.SuccessfulActionRequester;
import apiParts.skelethon.requests.crud.SuccessfulCrudRequester;
import apiParts.skelethon.requests.search.SuccessfulSearchRequester;
import apiParts.specs.RequestSpecs;
import apiParts.specs.ResponseSpecs;
import apiParts.testdata.AppointmentTestData;
import apiParts.utils.DateTimeUtils;

import java.util.List;
import java.util.Map;

public class AppointmentSteps {
    public static CreateAppointmentResponse createAppointment(CreateAppointmentRequest request) {
        return new SuccessfulCrudRequester<CreateAppointmentResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.APPOINTMENT_POST,
                ResponseSpecs.requestReturnsOk()
        ).create(request);
    }

    public static CreateAppointmentResponse createAppointment() {
        return createAppointment(
                RandomModelGenerator.generate(CreateAppointmentRequest.class)
        );
    }

    public static CreateAppointmentResponse createAppointment(String patientUUID) {
        CreateAppointmentRequest request =
                RandomModelGenerator.generate(CreateAppointmentRequest.class);
        request.setPatientUuid(patientUUID);

        return createAppointment(request);
    }

    public static CreateAppointmentResponse cancelAppointment(
            String appointmentUUID,
            AppointmentStatusChangeRequest request) {

        // POST /appointments/{uuid}/status-change - a command on the appointment, not CRUD
        return new SuccessfulActionRequester<CreateAppointmentResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.APPOINTMENT_CHANGE_STATUS,
                ResponseSpecs.requestReturnsOk()
        )
                .perform(appointmentUUID, request);
    }

    public static CreateAppointmentResponse cancelAppointment(String appointmentUUID) {
        return cancelAppointment(appointmentUUID, AppointmentTestData.cancelStatusChangeRequest());
    }

    public static CreateAppointmentResponse updateAppointment(
            UpdateAppointmentRequest request) {

        // The appointments module updates via POST /appointment with the uuid INSIDE the body -
        // the same request as create, so CrudEndpoint.update(uuid, ...) (POST /appointment/{uuid}) does not fit.
        return new SuccessfulCrudRequester<CreateAppointmentResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.APPOINTMENT_POST,
                ResponseSpecs.requestReturnsOk()
        )
                .create(request);
    }

    // POST /appointments/search answers a bare JSON array (no {"results": [...]} wrapper);
    // SuccessfulSearchRequester recognises that shape by itself
    public static List<CreateAppointmentResponse> searchAppointments(String patientUUID) {
        return new SuccessfulSearchRequester<CreateAppointmentResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.APPOINTMENTS_SEARCH,
                ResponseSpecs.requestReturnsOk()
        )
                .searchByBody(AppointmentTestData.searchRequest(patientUUID))
                .results();
    }
    public static List<CreateAppointmentResponse> getAppointments(String forDate) {
        SearchParams appointments = () -> Map.<String, Object>of(
                "forDate", forDate
        );

        return new SuccessfulSearchRequester<CreateAppointmentResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.APPOINTMENTS_GET,
                ResponseSpecs.requestReturnsOk()
        ).search(appointments).results();
    }
    public static CreateAppointmentResponse getAppointmentByUuid(
            String appointmentUUID,
            String startDateTime) {

        return getAppointments(DateTimeUtils.toForDate(startDateTime)).stream()
                .filter(appointment -> appointment.getUuid().equals(appointmentUUID))
                .findFirst()
                .orElseThrow(() -> new AssertionError(
                        "Appointment " + appointmentUUID +
                                " was not found in GET /appointments response"
                ));
    }
    public static CreateAppointmentResponse requireAppointment(
            List<CreateAppointmentResponse> appointments,
            String appointmentUUID) {

        return appointments.stream()
                .filter(appointment -> appointment.getUuid().equals(appointmentUUID))
                .findFirst()
                .orElseThrow(() -> new AssertionError(
                        "Created appointment was not found in search results"));
    }
}
