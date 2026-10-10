package apiParts.models.appointment;

import apiParts.models.BaseModel;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GetAppointmentSummaryResponse extends BaseModel {

    private AppointmentService appointmentService;
    private Map<String, AppointmentCount> appointmentCountMap;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AppointmentService {
        private Integer appointmentServiceId;
        private String name;
        private String uuid;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AppointmentCount {
        private Integer allAppointmentsCount;
        private Integer missedAppointmentsCount;
        private Long appointmentDate;
        private String appointmentServiceUuid;
    }
}