package uiParts.helpers;

import apiParts.models.appointment.AppointmentKind;
import apiParts.models.appointment.AppointmentService;
import apiParts.models.appointment.AppointmentStatus;
import apiParts.models.appointment.CreateAppointmentResponse;
import apiParts.steps.AppointmentSteps;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

public class AppointmentFinder {


    public static CreateAppointmentResponse findCreatedAppointment(
            String patientUUID,
            LocalDate appointmentDate,
            String appointmentNote
    ) {
        List<CreateAppointmentResponse> appointments =
                AppointmentSteps.searchAppointments(patientUUID);

        return appointments.stream()
                .filter(appointment -> appointment.getPatient() != null)
                .filter(appointment -> patientUUID.equals(
                        appointment.getPatient().getUuid()))
                .filter(appointment -> appointment.getStartDateTime() != null)
                .filter(appointment -> Instant
                        .ofEpochMilli(Long.parseLong(
                                appointment.getStartDateTime()))
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()
                        .equals(appointmentDate))
                .filter(appointment -> appointment.getService() != null)
                .filter(appointment -> AppointmentService.GENERAL_MEDICINE
                        .getDisplay().equals(
                                appointment.getService().getName()))
                .filter(appointment -> appointmentNote.equals(
                        appointment.getComments()))
                .filter(appointment -> AppointmentStatus.SCHEDULED.getValue()
                        .equals(appointment.getStatus()))
                .findFirst()
                .orElseThrow(() -> new AssertionError(
                        "Created appointment was not found for patient "
                                + patientUUID + " on " + appointmentDate
                ));
    }
}
