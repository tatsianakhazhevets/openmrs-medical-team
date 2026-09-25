package apiParts.models.appointment;

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
public class CreateAppointmentRequest extends BaseModel {
    private String uuid;
    private String appointmentKind;
    private String status;
    private String serviceUuid;
    private String startDateTime;
    private String endDateTime;
    private String locationUuid;
    private List<Provider> providers;
    private String patientUuid;
    private String comments;
    private String dateAppointmentScheduled;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Provider {
        private String uuid;
    }
}