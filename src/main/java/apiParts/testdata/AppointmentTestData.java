package apiParts.testdata;

import apiParts.generators.RandomModelGenerator;
import apiParts.models.appointment.AppointmentSearchRequest;
import apiParts.models.appointment.AppointmentStatusChangeRequest;
import apiParts.utils.DateTimeUtils;

/**
 * Requests of the standard appointment fixtures (see apiParts.steps.AdminSteps).
 */
public class AppointmentTestData {

    private static final int SEARCH_HISTORY_MONTHS = 6;

    private AppointmentTestData() {
    }

    // Cancels an appointment as of now, same fixed timezone the server instance runs in
    public static AppointmentStatusChangeRequest cancelStatusChangeRequest() {
        return RandomModelGenerator.generate(AppointmentStatusChangeRequest.class);
    }

    // Appointments of a patient over the last SEARCH_HISTORY_MONTHS
    public static AppointmentSearchRequest searchRequest(String patientUUID) {
        return AppointmentSearchRequest.builder()
                .patientUuid(patientUUID)
                .startDate(DateTimeUtils.nowMinusMonths(SEARCH_HISTORY_MONTHS).toInstant().toString())
                .build();
    }
}
