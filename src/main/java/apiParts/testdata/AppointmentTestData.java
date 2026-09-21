package apiParts.testdata;

import apiParts.generators.RandomModelGenerator;
import apiParts.models.Location;
import apiParts.models.appointment.AppointmentKind;
import apiParts.models.appointment.AppointmentProvider;
import apiParts.models.appointment.AppointmentSearchRequest;
import apiParts.models.appointment.AppointmentService;
import apiParts.models.appointment.AppointmentStatus;
import apiParts.models.appointment.AppointmentStatusChangeRequest;
import apiParts.models.appointment.CreateAppointmentRequest;
import apiParts.steps.AdminSteps;
import apiParts.utils.DateTimeUtils;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;

import static apiParts.utils.DateTimeUtils.OPENMRS_REQUEST_DATE_TIME;
import static apiParts.utils.DateTimeUtils.OPENMRS_RESPONSE_DATE_TIME;

/**
 * Requests of the standard appointment fixtures (see apiParts.steps.AdminSteps).
 */
public class AppointmentTestData {

    // Appointment starts shortly after now and lasts this long: any future slot is valid for the server
    private static final int START_IN_MINUTES = 30;
    private static final int DURATION_MINUTES = 30;
    // How far back appointment search looks for a patient's history
    private static final int SEARCH_HISTORY_MONTHS = 6;
    // Fixed timezone of the OpenMRS instance used to cancel an appointment "as of now"
    private static final ZoneId CANCEL_TIME_ZONE = ZoneId.of("Asia/Yerevan");

    private AppointmentTestData() {
    }

    // New scheduled appointment for a patient at Outpatient Clinic, starting in the near future.
    // Returns a builder so callers (e.g. negative-path tests) can override a field before build()
    public static CreateAppointmentRequest.CreateAppointmentRequestBuilder appointmentRequestBuilder(String patientUUID) {
        OffsetDateTime startDateTime = DateTimeUtils.nowPlusMinutes(START_IN_MINUTES);
        OffsetDateTime endDateTime = startDateTime.plusMinutes(DURATION_MINUTES);

        return CreateAppointmentRequest.builder()
                .appointmentKind(AppointmentKind.SCHEDULED.getValue())
                .status("")
                .serviceUuid(AppointmentService.GENERAL_MEDICINE.getUuid())
                .startDateTime(startDateTime.format(OPENMRS_REQUEST_DATE_TIME))
                .endDateTime(endDateTime.format(OPENMRS_REQUEST_DATE_TIME))
                .locationUuid(Location.OUTPATIENT_CLINIC.getUuid())
                .providers(List.of(
                        CreateAppointmentRequest.Provider.builder()
                                .uuid(AdminSteps.getCurrentProviderUuid())
                                .build()
                ))
                .patientUuid(patientUUID)
                .comments(RandomModelGenerator.randomSentence())
                .dateAppointmentScheduled(DateTimeUtils.now().format(OPENMRS_REQUEST_DATE_TIME));
    }

    public static CreateAppointmentRequest appointmentRequest(String patientUUID) {
        return appointmentRequestBuilder(patientUUID).build();
    }

    // Cancels an appointment as of now, same fixed timezone the server instance runs in
    public static AppointmentStatusChangeRequest cancelStatusChangeRequest() {
        return AppointmentStatusChangeRequest.builder()
                .toStatus(AppointmentStatus.CANCELLED.getValue())
                .onDate(OffsetDateTime.now().format(OPENMRS_RESPONSE_DATE_TIME))
                .timeZone(CANCEL_TIME_ZONE.getId())
                .build();
    }

    // Appointments of a patient over the last SEARCH_HISTORY_MONTHS
    public static AppointmentSearchRequest searchRequest(String patientUUID) {
        return AppointmentSearchRequest.builder()
                .patientUuid(patientUUID)
                .startDate(DateTimeUtils.nowMinusMonths(SEARCH_HISTORY_MONTHS).toInstant().toString())
                .build();
    }
}
