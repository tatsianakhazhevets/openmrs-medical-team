package apiTests;

import apiParts.generators.GenerationProfile;
import apiParts.generators.RandomModelGenerator;
import apiParts.models.EncounterType;
import apiParts.models.VitalsConcept;
import apiParts.models.encounter.CreateEncounterRequest;
import apiParts.models.encounter.CreateEncounterResponse;
import apiParts.models.patient.CreatePatientResponse;
import apiParts.skelethon.endpoints.Endpoint;
import apiParts.skelethon.requests.crud.CrudRequester;
import apiParts.skelethon.requests.crud.SuccessfulCrudRequester;
import apiParts.specs.RequestSpecs;
import apiParts.specs.ResponseSpecs;
import apiParts.steps.AdminSteps;
import apiParts.utils.DateTimeUtils;
import common.annotations.CreatePatient;
import common.storages.SessionStorage;
import net.datafaker.Faker;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static apiParts.models.errors.EncounterErrorMessages.*;

@CreatePatient
public class EncounterApiTests extends BaseTest {

    Faker faker = new Faker(new Locale("en", "US"));

    String nonExistingUuid = UUID.randomUUID().toString();
    String encounterDatetime = OffsetDateTime.now().withHour(10).withMinute(0).withSecond(0).withNano(0)
            .format(DateTimeUtils.OPENMRS_RESPONSE_DATE_TIME);
    String updatedEncounterDatetime = OffsetDateTime.parse(encounterDatetime, DateTimeUtils.OPENMRS_RESPONSE_DATE_TIME)
            .plusHours(1).format(DateTimeUtils.OPENMRS_RESPONSE_DATE_TIME);
    String updatedEncounterDatetimeExp = OffsetDateTime.parse(updatedEncounterDatetime, DateTimeUtils.OPENMRS_RESPONSE_DATE_TIME)
            .withOffsetSameInstant(ZoneOffset.UTC).format(DateTimeUtils.OPENMRS_RESPONSE_DATE_TIME);
    double normalTemperature = faker.number().randomDouble(1, 36, 37);

    @Test
    public void adminCanCreateEncounter() {
        String patientUUID = SessionStorage.getPatient().getUuid();

        CreateEncounterRequest createEncounterRequest = vitalsEncounter(fields("encounterDatetime", encounterDatetime));

        CreateEncounterResponse createdEncounterResponse =
                new SuccessfulCrudRequester<CreateEncounterResponse>(
                        RequestSpecs.adminSpec(),
                        Endpoint.ENCOUNTER_POST,
                        ResponseSpecs.requestReturnsCreated())
                        .create(createEncounterRequest);

        CreateEncounterResponse receivedEncounterResponse =
                new SuccessfulCrudRequester<CreateEncounterResponse>(
                        RequestSpecs.adminSpec(),
                        Endpoint.ENCOUNTER_GET,
                        ResponseSpecs.requestReturnsOk())
                        .get(createdEncounterResponse.getUuid(), Map.of("v", "full"));

        softly.assertThat(receivedEncounterResponse.getUuid()).isEqualTo(createdEncounterResponse.getUuid());
        softly.assertThat(receivedEncounterResponse.getPatient().getUuid()).isEqualTo(patientUUID);
        softly.assertThat(receivedEncounterResponse.getEncounterType().getUuid())
                .isEqualTo(EncounterType.VITALS.getUuid());
        softly.assertThat(receivedEncounterResponse.getLocation().getUuid())
                .isEqualTo(createEncounterRequest.getLocation().getUuid());
        softly.assertThat(receivedEncounterResponse.getVoided()).isFalse();
    }

    @Test
    public void adminCanCreateEncounterWithObservation() {
        String patientUUID = SessionStorage.getPatient().getUuid();

        CreateEncounterRequest createEncounterRequest = vitalsEncounter(fields(
                "encounterDatetime", encounterDatetime,
                "obs", List.of(CreateEncounterRequest.Obs.of(VitalsConcept.TEMPERATURE, normalTemperature))));

        CreateEncounterResponse createdEncounterResponse = new SuccessfulCrudRequester<CreateEncounterResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.ENCOUNTER_POST,
                ResponseSpecs.requestReturnsCreated())
                .create(createEncounterRequest);

        CreateEncounterResponse receivedEncounterResponse = new SuccessfulCrudRequester<CreateEncounterResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.ENCOUNTER_GET,
                ResponseSpecs.requestReturnsOk())
                .get(createdEncounterResponse.getUuid(), Map.of("v", "full"));

        softly.assertThat(receivedEncounterResponse.getUuid()).isEqualTo(createdEncounterResponse.getUuid());
        softly.assertThat(receivedEncounterResponse.getPatient().getUuid()).isEqualTo(patientUUID);
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
                        vitalsEncounter(fields("patient", null)),
                        MISSING_PATIENT.getMessage()
                ),

                Arguments.of(
                        // non-existing patient
                        vitalsEncounter(fields("patient", "00000000-0000-0000-0000-000000000000")),
                        INVALID_SUBMISSION.getMessage()
                )
        );
    }

    @ParameterizedTest
    @MethodSource("invalidPatientEncounterRequests")
    public void adminCannotCreateInvalidPatientEncounter(CreateEncounterRequest encounterRequest, String errorMessage) {

        new CrudRequester(
                RequestSpecs.adminSpec(),
                Endpoint.ENCOUNTER_POST,
                ResponseSpecs.requestReturnsBadRequestWithMessage(errorMessage))
                .create(encounterRequest);
    }

    // @MethodSource providers run before any per-test extension (e.g. @CreatePatient's
    // BeforeEachCallback), so a dedicated patient is created here rather than relying on SessionStorage
    private static Stream<Arguments> invalidTypesEncounterRequests() {

        CreatePatientResponse patient = AdminSteps.createPatient();

        return Stream.of(
                Arguments.of(
                        // without encounter type
                        vitalsEncounter(fields("patient", patient.getUuid(), "encounterType", null)),
                        MISSING_ENCOUNTER_TYPE.getMessage()
                ),

                Arguments.of(
                        // future datetime
                        vitalsEncounter(fields(
                                "patient", patient.getUuid(),
                                "encounterDatetime", "2099-01-01T10:00:00.000+0000")),
                        INVALID_SUBMISSION.getMessage()
                )
        );
    }

    @ParameterizedTest
    @MethodSource("invalidTypesEncounterRequests")
    public void adminCannotCreateInvalidEncounter(CreateEncounterRequest encounterRequest, String errorMessage) {

        new CrudRequester(
                RequestSpecs.adminSpec(),
                Endpoint.ENCOUNTER_POST,
                ResponseSpecs.requestReturnsBadRequestWithMessage(errorMessage))
                .create(encounterRequest);
    }

    @Test
    public void adminCannotGetNonExistingEncounter() {
        new CrudRequester(
                RequestSpecs.adminSpec(),
                Endpoint.ENCOUNTER_GET,
                ResponseSpecs.requestReturnsNotFound(
                        OBJECT_WITH_UUID_DOES_NOT_EXIST.getMessage()))
                .get(nonExistingUuid, Map.of("v", "full"));
    }

    @Test
    public void adminCanUpdateEncounter() {
        String patientUUID = SessionStorage.getPatient().getUuid();

        CreateEncounterRequest createRequest = vitalsEncounter(fields("encounterDatetime", encounterDatetime));

        CreateEncounterResponse createdEncounterResponse = new SuccessfulCrudRequester<CreateEncounterResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.ENCOUNTER_POST,
                ResponseSpecs.requestReturnsCreated())
                .create(createRequest);

        CreateEncounterRequest updateRequest = CreateEncounterRequest.builder()
                .encounterDatetime(updatedEncounterDatetime)
                .build();

        CreateEncounterResponse updatedEncounterResponse =
                new SuccessfulCrudRequester<CreateEncounterResponse>(
                        RequestSpecs.adminSpec(),
                        Endpoint.ENCOUNTER_CRUD,
                        ResponseSpecs.requestReturnsOk())
                        .update(createdEncounterResponse.getUuid(), updateRequest);
        softly.assertThat(updatedEncounterResponse.getUuid()).isEqualTo(createdEncounterResponse.getUuid());
        softly.assertThat(updatedEncounterResponse.getEncounterDatetime()).isEqualTo(updatedEncounterDatetimeExp);
        softly.assertThat(updatedEncounterResponse.getPatient().getUuid()).isEqualTo(patientUUID);
        softly.assertThat(updatedEncounterResponse.getEncounterType().getUuid())
                .isEqualTo(EncounterType.VITALS.getUuid());
        softly.assertThat(updatedEncounterResponse.getVoided()).isFalse();
    }

    @Test
    public void adminCannotUpdateNonExistingEncounter() {
        CreateEncounterRequest updateRequest = CreateEncounterRequest.builder()
                .encounterDatetime(encounterDatetime)
                .build();

        new CrudRequester(
                RequestSpecs.adminSpec(),
                Endpoint.ENCOUNTER_CRUD,
                ResponseSpecs.requestReturnsNotFound(
                        OBJECT_WITH_UUID_DOES_NOT_EXIST.getMessage()))
                .update(nonExistingUuid, updateRequest);
    }

    @Test
    public void adminCanDeleteEncounter() {
        String patientUUID = SessionStorage.getPatient().getUuid();

        CreateEncounterRequest encounterRequest = vitalsEncounter(fields("encounterDatetime", encounterDatetime));

        CreateEncounterResponse createdEncounterResponse = new SuccessfulCrudRequester<CreateEncounterResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.ENCOUNTER_POST,
                ResponseSpecs.requestReturnsCreated())
                .create(encounterRequest);

        new SuccessfulCrudRequester<CreateEncounterResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.ENCOUNTER_DELETE,
                ResponseSpecs.requestReturnsNoContent())
                .delete(createdEncounterResponse.getUuid());

        CreateEncounterResponse deletedEncounterResponse = new SuccessfulCrudRequester<CreateEncounterResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.ENCOUNTER_GET,
                ResponseSpecs.requestReturnsOk())
                .get(createdEncounterResponse.getUuid(), Map.of("v", "full"));

        softly.assertThat(deletedEncounterResponse.getUuid()).isEqualTo(createdEncounterResponse.getUuid());
        softly.assertThat(deletedEncounterResponse.getVoided()).isTrue();
    }

    @Test
    public void adminCannotDeleteNonExistingEncounter() {
        new CrudRequester(
                RequestSpecs.adminSpec(),
                Endpoint.ENCOUNTER_DELETE,
                ResponseSpecs.requestReturnsNotFound(
                        OBJECT_WITH_UUID_DOES_NOT_EXIST.getMessage()))
                .delete(nonExistingUuid);
    }

    // Valid vitals encounter of the @CreatePatient patient, overrides replace generated fields
    private static CreateEncounterRequest vitalsEncounter(Map<String, Object> overrides) {
        return RandomModelGenerator.generate(CreateEncounterRequest.class, GenerationProfile.VITALS, overrides);
    }

    // Overrides as key-value pairs. null value = field is omitted from the request (Map.of does not allow null)
    private static Map<String, Object> fields(Object... keyValues) {
        Map<String, Object> overrides = new HashMap<>();
        for (int i = 0; i < keyValues.length; i += 2) {
            overrides.put((String) keyValues[i], keyValues[i + 1]);
        }
        return overrides;
    }
}
