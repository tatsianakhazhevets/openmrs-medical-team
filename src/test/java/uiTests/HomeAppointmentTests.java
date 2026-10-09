package uiTests;

import apiParts.generators.RandomModelGenerator;
import apiParts.models.Location;
import apiParts.models.appointment.AppointmentKind;
import apiParts.models.appointment.AppointmentService;
import apiParts.models.appointment.AppointmentStatus;
import apiParts.models.appointment.CreateAppointmentResponse;
import apiParts.models.patient.CreatePatientResponse;
import apiParts.specs.RequestSpecs;
import apiParts.steps.AppointmentSteps;
import common.annotations.CreatePatient;
import common.storages.SessionStorage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uiParts.helpers.AppointmentDates;
import uiParts.helpers.AppointmentFinder;
import uiParts.pages.BasePage;
import uiParts.pages.HomeAppointmentsPage;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;

@CreatePatient
public class HomeAppointmentTests extends BaseUiTest {
    private String patientUUID;
    private String appointmentUUID;
    private final LocalDate randomAppointmentDate = AppointmentDates.randomAppointmentDate();
    private final LocalDate randomIssuedDate = AppointmentDates.randomIssuedDate(randomAppointmentDate);
    private final String appointmentNote = RandomModelGenerator.randomWord();
    private final int appointmentDuration = RandomModelGenerator.randomInt(1, 10);

    @BeforeEach
    void setUp() {
        patientUUID = SessionStorage.getPatient().getUuid();
    }

    @AfterEach
    void cancelCreatedAppointment() {
        if (appointmentUUID != null) {
            AppointmentSteps.cancelAppointment(appointmentUUID);
        }
    }

    @Test
    public void shouldCreateAppointment() {
        BasePage.authAsUser(RequestSpecs.ADMIN_USERNAME, RequestSpecs.ADMIN_PASSWORD);

        CreatePatientResponse patient = SessionStorage.getPatient();

        String patientName = patient.getPerson()
                .getDisplay();
        String patientIdentifier = patient.getIdentifiers()
                .get(0)
                .getDisplay()
                .replace("OpenMRS ID = ", "");


        new HomeAppointmentsPage()
                .open()
                .clickCreateAppointment()
                .searchPatient(patientName)
                .clickPatientCard(patientName)
                .shouldHavePatientName(patientName)
                .selectLocation(Location.OUTPATIENT_CLINIC.getDisplay())
                .selectService(AppointmentService.GENERAL_MEDICINE.getDisplay())
                .selectAppointmentType(AppointmentKind.SCHEDULED.getValue())
                .setDuration(appointmentDuration)
                .selectAppointmentDate(randomAppointmentDate)
                .shouldHaveAppointmentDate(randomAppointmentDate)
                .selectIssuedDate(randomIssuedDate)
                .shouldHaveIssuedDate(randomIssuedDate)
                .setNote(appointmentNote)
                .saveAppointment()
                .selectScheduleDate(randomAppointmentDate)
                .shouldHaveAppointment(patientName, patientIdentifier, Location.OUTPATIENT_CLINIC.getDisplay(), AppointmentService.GENERAL_MEDICINE.getDisplay(), AppointmentKind.SCHEDULED.getValue());
        CreateAppointmentResponse appointment =
                AppointmentFinder.findCreatedAppointment(
                        patientUUID,
                        randomAppointmentDate,
                        appointmentNote
                );

        appointmentUUID = appointment.getUuid();

        assertThat(appointment.getUuid()).isNotBlank();
        assertThat(appointment.getPatient().getUuid()).isEqualTo(patientUUID);
        assertThat(appointment.getLocation().getName()).isEqualTo(Location.OUTPATIENT_CLINIC.getDisplay());
        assertThat(appointment.getService().getName()).isEqualTo(AppointmentService.GENERAL_MEDICINE.getDisplay());
        assertThat(appointment.getAppointmentKind()).isEqualTo(AppointmentKind.SCHEDULED.getValue());
        assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.SCHEDULED.getValue());
        assertThat(appointment.getComments()).isEqualTo(appointmentNote);
        LocalDate actualDate = Instant.ofEpochMilli(Long.parseLong(appointment.getStartDateTime()))
                .atZone(ZoneId.systemDefault()).toLocalDate();
        assertThat(actualDate).isEqualTo(randomAppointmentDate);
        long actualDurationMinutes =
                (Long.parseLong(appointment.getEndDateTime())
                        - Long.parseLong(appointment.getStartDateTime())) / 60_000;
        assertThat(actualDurationMinutes).isEqualTo(appointmentDuration);
    }
    @Test
    void shouldShowNoResultsForUnknownPatient() {
        String unknownPatientName = RandomModelGenerator.randomWord()
                + RandomModelGenerator.randomUnknownUuid();
        BasePage.authAsUser(RequestSpecs.ADMIN_USERNAME, RequestSpecs.ADMIN_PASSWORD);

        new HomeAppointmentsPage()
                .open()
                .clickCreateAppointment()
                .searchPatient(unknownPatientName)
                .shouldShowNoSearchResults();
    }
}
