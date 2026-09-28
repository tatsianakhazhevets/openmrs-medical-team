package apiParts.models.appointment;

import apiParts.generators.*;
import apiParts.models.BaseModel;
import apiParts.models.Location;
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

    @IgnoreGeneratingRule
    private String uuid;

    @FixedEnumGeneratingRule(
            enumClass = AppointmentKind.class,
            valueMethod = "getValue",
            value = "SCHEDULED"
    )
    private String appointmentKind;

    @FixedEnumGeneratingRule(
            enumClass = AppointmentStatus.class,
            valueMethod = "getValue",
            value = "CHECKED_IN"
    )
    private String status;

    @EnumGeneratingRule(
            enumClass = AppointmentService.class,
            valueMethod = "getUuid"
    )
    private String serviceUuid;

    @DateTimeGeneratingRule(minutesFromNow = 60)
    private String startDateTime;

    @DateTimeGeneratingRule(
            baseField = "startDateTime",
            minutesFromBase = 30
    )
    private String endDateTime;

    @EnumGeneratingRule(
            enumClass = Location.class,
            valueMethod = "getUuid"
    )
    private String locationUuid;

    @CollectionGeneratingRule(minSize = 1, maxSize = 1)
    private List<CreateAppointmentRequest.Provider> providers;

    @PatientUuidGeneratingRule
    private String patientUuid;

    @StringGeneratingRule(regex = "[A-Za-z ]{5,100}")
    private String comments;

    @DateGeneratingRule(minYear = 2026, maxYear = 2026)
    private String dateAppointmentScheduled;
}
