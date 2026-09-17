package apiTests;

import apiParts.models.EncounterType;
import apiParts.models.Location;
import apiParts.models.VitalsConcept;
import apiParts.models.encounter.CreateEncounterRequest;
import apiParts.models.encounter.CreateEncounterResponse;
import apiParts.models.patient.CreatePatientResponse;
import apiParts.skelethon.endpoints.Endpoint;
import apiParts.skelethon.requests.encounter.EncounterRequester;
import apiParts.skelethon.requests.encounter.SuccessfulEncounterRequester;
import apiParts.specs.RequestSpecs;
import apiParts.specs.ResponseSpecs;
import apiParts.steps.AdminSteps;
import net.datafaker.Faker;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Stream;

import static apiParts.models.errors.EncounterErrorMessages.*;

public class EncounterApiTests extends BaseTest {

    Faker faker = new Faker(new Locale("en", "US"));

    String nonExistingUuid = UUID.randomUUID().toString();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    String encounterDatetime = OffsetDateTime.now().withHour(10).withMinute(0).withSecond(0).withNano(0).format(formatter);
    String updatedEncounterDatetime = OffsetDateTime.parse(encounterDatetime, formatter).plusHours(1).format(formatter);
    String updatedEncounterDatetimeExp = OffsetDateTime.parse(updatedEncounterDatetime, formatter).withOffsetSameInstant(ZoneOffset.UTC).format(formatter);
    double normalTemperature = faker.number().randomDouble(1, 36, 37);

    @Test
    public void adminCanCreateEncounter() {
        CreatePatientResponse patient = AdminSteps.createPatient();

        CreateEncounterRequest createEncounterRequest = CreateEncounterRequest.builder()
                .patient(patient.getUuid())
                .encounterType(EncounterType.VITALS)
                .encounterDatetime(encounterDatetime)
                .location(Location.OUTPATIENT_CLINIC)
                .build();

        CreateEncounterResponse createdEncounterResponse =
                new SuccessfulEncounterRequester<CreateEncounterResponse>(
                        RequestSpecs.adminSpec(),
                        Endpoint.ENCOUNTER_POST,
                        ResponseSpecs.requestReturnsCreated())
                        .create(createEncounterRequest);

        CreateEncounterResponse receivedEncounterResponse =
                new SuccessfulEncounterRequester<CreateEncounterResponse>(
                        RequestSpecs.adminSpec(),
                        Endpoint.ENCOUNTER_GET,
                        ResponseSpecs.requestReturnsOk())
                        .get(createdEncounterResponse.getUuid());

        softly.assertThat(receivedEncounterResponse.getUuid()).isEqualTo(createdEncounterResponse.getUuid());
        softly.assertThat(receivedEncounterResponse.getPatient().getUuid()).isEqualTo(patient.getUuid());
        softly.assertThat(receivedEncounterResponse.getEncounterType().getUuid())
                .isEqualTo(EncounterType.VITALS.getUuid());
        softly.assertThat(receivedEncounterResponse.getLocation().getUuid())
                .isEqualTo(Location.OUTPATIENT_CLINIC.getUuid());
        softly.assertThat(receivedEncounterResponse.getVoided()).isFalse();
    }

    @Test
    public void adminCanCreateEncounterWithObservation() {
        CreatePatientResponse patient = AdminSteps.createPatient();

        CreateEncounterRequest createEncounterRequest = CreateEncounterRequest.builder()
                .patient(patient.getUuid())
                .encounterType(EncounterType.VITALS)
                .encounterDatetime(encounterDatetime)
                .location(Location.OUTPATIENT_CLINIC)
                .obs(List.of(CreateEncounterRequest.Obs.of(VitalsConcept.TEMPERATURE, normalTemperature)))
                .build();

        CreateEncounterResponse createdEncounterResponse = new SuccessfulEncounterRequester<CreateEncounterResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.ENCOUNTER_POST,
                ResponseSpecs.requestReturnsCreated())
                .create(createEncounterRequest);

        CreateEncounterResponse receivedEncounterResponse = new SuccessfulEncounterRequester<CreateEncounterResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.ENCOUNTER_GET,
                ResponseSpecs.requestReturnsOk())
                .get(createdEncounterResponse.getUuid());

        softly.assertThat(receivedEncounterResponse.getUuid()).isEqualTo(createdEncounterResponse.getUuid());
        softly.assertThat(receivedEncounterResponse.getPatient().getUuid()).isEqualTo(patient.getUuid());
        softly.assertThat(receivedEncounterResponse.getObs()).hasSize(1);
        softly.assertThat(receivedEncounterResponse.getObs().get(0).getUuid())
                .isEqualTo(createdEncounterResponse.getObs().get(0).getUuid());
        softly.assertThat(receivedEncounterResponse.getObs().get(0).getDisplay()).contains(Double.toString(normalTemperature));
        softly.assertThat(receivedEncounterResponse.getVoided()).isFalse();
    }


    private static Stream<Arguments> invalidPatientEncounterRequests() {

        return Stream.of(
                Arguments.of(
                        // without patient
                        CreateEncounterRequest.builder()
                                .encounterType(EncounterType.VITALS)
                                .encounterDatetime("2026-09-16T10:00:00.000+0200")
                                .location(Location.OUTPATIENT_CLINIC)
                                .build(),
                        MISSING_PATIENT.getMessage()
                ),

                Arguments.of(
                        // non-existing patient
                        CreateEncounterRequest.builder()
                                .patient("00000000-0000-0000-0000-000000000000")
                                .encounterType(EncounterType.VITALS)
                                .encounterDatetime("2026-09-16T10:00:00.000+0200")
                                .location(Location.OUTPATIENT_CLINIC)
                                .build(),
                        INVALID_SUBMISSION.getMessage()
                )
        );
    }

    @ParameterizedTest
    @MethodSource("invalidPatientEncounterRequests")
    public void adminCannotCreateInvalidPatientEncounter(CreateEncounterRequest encounterRequest, String errorMessage) {

        new EncounterRequester(
                RequestSpecs.adminSpec(),
                Endpoint.ENCOUNTER_POST,
                ResponseSpecs.requestReturnsBadRequestWithMessage(errorMessage))
                .create(encounterRequest);
    }

    private static Stream<Arguments> invalidTypesEncounterRequests() {

        CreatePatientResponse patient = AdminSteps.createPatient();

        return Stream.of(
                Arguments.of(
                        // without encounter type
                        CreateEncounterRequest.builder()
                                .patient(patient.getUuid())
                                .encounterDatetime("2026-09-16T10:00:00.000+0200")
                                .location(Location.OUTPATIENT_CLINIC)
                                .build(),
                        MISSING_ENCOUNTER_TYPE.getMessage()
                ),

                Arguments.of(
                        // future datetime
                        CreateEncounterRequest.builder()
                                .patient(patient.getUuid())
                                .encounterType(EncounterType.VITALS)
                                .encounterDatetime("2099-01-01T10:00:00.000+0000")
                                .location(Location.OUTPATIENT_CLINIC)
                                .build(),
                        INVALID_SUBMISSION.getMessage()
                )
        );
    }

    @ParameterizedTest
    @MethodSource("invalidTypesEncounterRequests")
    public void adminCannotCreateInvalidEncounter(CreateEncounterRequest encounterRequest, String errorMessage) {

        new EncounterRequester(
                RequestSpecs.adminSpec(),
                Endpoint.ENCOUNTER_POST,
                ResponseSpecs.requestReturnsBadRequestWithMessage(errorMessage))
                .create(encounterRequest);
    }

    @Test
    public void adminCannotGetNonExistingEncounter() {
        new EncounterRequester(
                RequestSpecs.adminSpec(),
                Endpoint.ENCOUNTER_GET,
                ResponseSpecs.requestReturnsNotFound(
                        OBJECT_WITH_UUID_DOES_NOT_EXIST.getMessage()))
                .get(nonExistingUuid);
    }

    @Test
    public void adminCanUpdateEncounter() {
        CreatePatientResponse patient = AdminSteps.createPatient();

        CreateEncounterRequest createRequest = CreateEncounterRequest.builder()
                .patient(patient.getUuid())
                .encounterType(EncounterType.VITALS)
                .encounterDatetime(encounterDatetime)
                .location(Location.OUTPATIENT_CLINIC)
                .build();

        CreateEncounterResponse createdEncounterResponse = new SuccessfulEncounterRequester<CreateEncounterResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.ENCOUNTER_POST,
                ResponseSpecs.requestReturnsCreated())
                .create(createRequest);

        CreateEncounterRequest updateRequest = CreateEncounterRequest.builder()
                .encounterDatetime(updatedEncounterDatetime)
                .build();

        CreateEncounterResponse updatedEncounterResponse =
                new SuccessfulEncounterRequester<CreateEncounterResponse>(
                        RequestSpecs.adminSpec(),
                        Endpoint.ENCOUNTER_UPDATE,
                        ResponseSpecs.requestReturnsOk())
                        .update(createdEncounterResponse.getUuid(), updateRequest);
        softly.assertThat(updatedEncounterResponse.getUuid()).isEqualTo(createdEncounterResponse.getUuid());
        softly.assertThat(updatedEncounterResponse.getEncounterDatetime()).isEqualTo(updatedEncounterDatetimeExp);
        softly.assertThat(updatedEncounterResponse.getPatient().getUuid()).isEqualTo(patient.getUuid());
        softly.assertThat(updatedEncounterResponse.getEncounterType().getUuid())
                .isEqualTo(EncounterType.VITALS.getUuid());
        softly.assertThat(updatedEncounterResponse.getVoided()).isFalse();
    }

    @Test
    public void adminCannotUpdateNonExistingEncounter() {
        CreateEncounterRequest updateRequest = CreateEncounterRequest.builder()
                .encounterDatetime(encounterDatetime)
                .build();

        new EncounterRequester(
                RequestSpecs.adminSpec(),
                Endpoint.ENCOUNTER_UPDATE,
                ResponseSpecs.requestReturnsNotFound(
                        OBJECT_WITH_UUID_DOES_NOT_EXIST.getMessage()))
                .update(nonExistingUuid, updateRequest);
    }

    @Test
    public void adminCanDeleteEncounter() {
        CreatePatientResponse patient = AdminSteps.createPatient();

        CreateEncounterRequest encounterRequest = CreateEncounterRequest.builder()
                .patient(patient.getUuid())
                .encounterType(EncounterType.VITALS)
                .encounterDatetime(encounterDatetime)
                .location(Location.OUTPATIENT_CLINIC)
                .build();

        CreateEncounterResponse createdEncounterResponse = new SuccessfulEncounterRequester<CreateEncounterResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.ENCOUNTER_POST,
                ResponseSpecs.requestReturnsCreated())
                .create(encounterRequest);

        new SuccessfulEncounterRequester<CreateEncounterResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.ENCOUNTER_DELETE,
                ResponseSpecs.requestReturnsNoContent())
                .delete(createdEncounterResponse.getUuid());

        CreateEncounterResponse deletedEncounterResponse = new SuccessfulEncounterRequester<CreateEncounterResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.ENCOUNTER_GET,
                ResponseSpecs.requestReturnsOk())
                .get(createdEncounterResponse.getUuid());

        softly.assertThat(deletedEncounterResponse.getUuid()).isEqualTo(createdEncounterResponse.getUuid());
        softly.assertThat(deletedEncounterResponse.getVoided()).isTrue();
    }

    @Test
    public void adminCannotDeleteNonExistingEncounter() {
        new EncounterRequester(
                RequestSpecs.adminSpec(),
                Endpoint.ENCOUNTER_DELETE,
                ResponseSpecs.requestReturnsNotFound(
                        OBJECT_WITH_UUID_DOES_NOT_EXIST.getMessage()))
                .delete(nonExistingUuid);
    }
}