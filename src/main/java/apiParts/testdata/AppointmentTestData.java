package apiParts.testdata;

import apiParts.models.appointment.AppointmentSearchRequest;
import apiParts.models.appointment.AppointmentStatus;
import apiParts.models.appointment.AppointmentStatusChangeRequest;
import apiParts.utils.DateTimeUtils;

import static apiParts.utils.DateTimeUtils.OPENMRS_RESPONSE_DATE_TIME;

/**
 * Requests of the standard appointment fixtures (see apiParts.steps.AdminSteps).
 */
public class AppointmentTestData {

    private static final int SEARCH_HISTORY_MONTHS = 6;

    private AppointmentTestData() {
    }

    // Cancels an appointment as of now, same fixed timezone the server instance runs in
    public static AppointmentStatusChangeRequest cancelStatusChangeRequest() {
        return AppointmentStatusChangeRequest.builder()
                .toStatus(AppointmentStatus.CANCELLED.getValue())
                .onDate(DateTimeUtils.now().format(OPENMRS_RESPONSE_DATE_TIME))
                .timeZone(DateTimeUtils.MOSCOW.getId())
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
