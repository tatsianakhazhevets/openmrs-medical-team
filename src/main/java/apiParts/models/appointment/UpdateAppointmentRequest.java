package apiParts.models.appointment;

import apiParts.generators.*;
import apiParts.models.BaseModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAppointmentRequest extends BaseModel {

    @GeneratingRule(nullable = true)
    private String uuid;

    @GeneratingRule(property = "test_appointment_kind")
    private String appointmentKind;

    @GeneratingRule(property = "test_appointment_update_status")
    private String status;

    @GeneratingRule(property = "test_appointment_service_uuid")
    private String serviceUuid;

    @GeneratingRule(strategy = GenerationStrategy.DATE_TIME, minutesFromNow = 60)
    private String startDateTime;

    @GeneratingRule(
            strategy = GenerationStrategy.DATE_TIME,
            baseField = "startDateTime",
            minutesFromBase = 30
    )
    private String endDateTime;

    @GeneratingRule(property = "test_location_uuid")
    private String locationUuid;

    @GeneratingRule(minSize = 1, maxSize = 1)
    private List<CreateAppointmentRequest.Provider> providers;

    @GeneratingRule(strategy = GenerationStrategy.PATIENT_UUID)
    private String patientUuid;

    @GeneratingRule(regex = "[A-Za-z ]{5,100}")
    private String comments;

    @GeneratingRule(strategy = GenerationStrategy.DATE, minYear = 2026, maxYear = 2026)
    private String dateAppointmentScheduled;
}
