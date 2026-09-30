package apiParts.models.appointment;

import apiParts.generators.GeneratingRule;
import apiParts.generators.GenerationStrategy;
import apiParts.models.BaseModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentStatusChangeRequest extends BaseModel {
    @GeneratingRule(property = "test_appointment_cancel_status")
    private String toStatus;

    @GeneratingRule(strategy = GenerationStrategy.DATE_TIME)
    private String onDate;

    @GeneratingRule(property = "test_time_zone")
    private String timeZone;
}