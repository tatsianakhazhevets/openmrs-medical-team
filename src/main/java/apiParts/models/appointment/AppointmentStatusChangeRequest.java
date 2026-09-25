package apiParts.models.appointment;

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
    private String toStatus;
    private String onDate;
    private String timeZone;
}