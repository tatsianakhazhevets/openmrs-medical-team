package apiParts.models.appointment;

import apiParts.models.BaseModel;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateAppointmentResponse extends BaseModel {
    private String uuid;
    private String appointmentNumber;
    private String dateAppointmentScheduled;

    private AppointmentPatient patient;
    private AppointmentService service;
    private AppointmentLocation location;

    private String startDateTime;
    private String endDateTime;
    private String appointmentKind;
    private String status;
    private String comments;

    private List<AppointmentProvider> providers;

    private Boolean voided;
    private Boolean recurring;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AppointmentPatient {
        private String uuid;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AppointmentService {
        private String uuid;
        private String name;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AppointmentLocation {
        private String uuid;
        private String name;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AppointmentProvider {
        private String uuid;
        private String name;
        private String response;
    }
}