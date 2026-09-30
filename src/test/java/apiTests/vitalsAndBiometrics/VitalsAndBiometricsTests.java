package apiTests.vitalsAndBiometrics;

import apiParts.assertions.ModelAssertions;
import apiParts.generators.RandomModelGenerator;
import apiParts.models.encounter.EncounterType;
import apiParts.models.encounter.EncounterResponse;
import apiParts.models.errors.ObsFieldError;
import apiParts.models.vitals.CreateVitalsRequest;
import apiParts.models.vitals.Obs;
import apiParts.skelethon.endpoints.Endpoint;
import apiParts.skelethon.requests.crud.CrudRequester;
import apiParts.skelethon.requests.crud.SuccessfulCrudRequester;
import apiParts.specs.RequestSpecs;
import apiParts.specs.ResponseSpecs;
import apiParts.steps.VitalsSteps;
import apiParts.utils.Uuids;
import apiTests.BaseTest;
import common.annotations.CreateEncounter;
import common.annotations.CreatePatient;
import common.storages.SessionStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@CreatePatient
public class VitalsAndBiometricsTests extends BaseTest {
    private String patientUUID;

    @BeforeEach
    void setUp() {
        patientUUID = SessionStorage.getPatient().getUuid();
    }

    // Boundaries = lowAbsolute / hiAbsolute of the concept reference range (inclusive, ObsValidator),
    // kept in VitalsConcept. Check: GET /obs?patient={uuid}&concept={uuid}&v=full -> referenceRange
    static Stream<Arguments> validVitalsBoundaries() {
        return Stream.of(
                Arguments.of("lower boundary", VitalsSteps.obsAtLowerBoundary()),
                Arguments.of("upper boundary", VitalsSteps.obsAtUpperBoundary())
        );
    }

    // Same boundaries as in validVitalsBoundaries(), one step outside: each obs is sent alone,
    // because 400 response does not say which obs is out of range.
    // Concepts without absolute limits (MID_UPPER_ARM_CIRC, TEXT) have no negative cases
    static Stream<Arguments> outOfRangeVitals() {
        return Stream.concat(
                VitalsSteps.obsBelowLowerBoundary().stream()
                        .map(obs -> Arguments.of("below lower boundary", obs, ObsFieldError.VALUE_OUT_OF_RANGE_LOW)),
                VitalsSteps.obsAboveUpperBoundary().stream()
                        .map(obs -> Arguments.of("above upper boundary", obs, ObsFieldError.VALUE_OUT_OF_RANGE_HIGH))
        );
    }

    @Test
    public void adminCanAddVitals() {
        var request = RandomModelGenerator.generate(CreateVitalsRequest.class);

        var encounter = new SuccessfulCrudRequester<EncounterResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.ENCOUNTER_POST,
                ResponseSpecs.requestReturnsCreated()
        )
                .create(request);
        // obs in POST /encounter response are refs (uuid + display) - values are checked via GET /obs
        ModelAssertions.assertThatModels(request, encounter)
                .as("POST /encounter response")
                .match();
        softly.assertThat(encounter.getObs())
                .as("obs in POST /encounter response")
                .hasSize(request.getObs().size());

        var patientObs = VitalsSteps.getPatientObs(patientUUID);

        ModelAssertions.assertThatModels(request.getObs(), patientObs.results())
                .as("obs saved for patient")
                .match();
        softly.assertThat(patientObs.results().stream().map(o -> o.getPerson().getUuid()).toList())
                .as("obs person is the encounter patient")
                .containsOnly(request.getPatient());
        softly.assertThat(Uuids.of(patientObs.results()))
                .as("obs uuids from GET match POST /encounter")
                .isEqualTo(Uuids.of(encounter.getObs()));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("validVitalsBoundaries")
    public void adminCanAddVitalsOnValidBoundaries(String boundary, List<Obs> obs){

        var request = RandomModelGenerator.generate(CreateVitalsRequest.class);
        request.setObs(obs);

        var encounter = new SuccessfulCrudRequester<EncounterResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.ENCOUNTER_POST,
                ResponseSpecs.requestReturnsCreated()
        )
                .create(request);
        // obs in POST /encounter response are refs (uuid + display) - values are checked via GET /obs
        ModelAssertions.assertThatModels(request, encounter)
                .as("POST /encounter response")
                .match();
        softly.assertThat(encounter.getObs())
                .as("obs in POST /encounter response")
                .hasSize(request.getObs().size());

        var patientObs = VitalsSteps.getPatientObs(patientUUID);

        ModelAssertions.assertThatModels(request.getObs(), patientObs.results())
                .as("obs saved for patient")
                .match();
        softly.assertThat(patientObs.results().stream().map(o -> o.getPerson().getUuid()).toList())
                .as("obs person is the encounter patient")
                .containsOnly(request.getPatient());
        softly.assertThat(Uuids.of(patientObs.results()))
                .as("obs uuids from GET match POST /encounter")
                .isEqualTo(Uuids.of(encounter.getObs()));
    }

    @ParameterizedTest(name = "{0}: {1} -> {2}")
    @MethodSource("outOfRangeVitals")
    public void adminCannotAddVitalsOutOfRange(String boundary, Obs obs, ObsFieldError error) {

        var request = RandomModelGenerator.generate(CreateVitalsRequest.class);
        request.setObs(List.of(obs));
        var before = VitalsSteps.getPatientObs(patientUUID).results();

        new CrudRequester(
                RequestSpecs.adminSpec(),
                Endpoint.ENCOUNTER_POST,
                ResponseSpecs.requestReturnsInvalidSubmission(error)
        )
                .create(request);

        ModelAssertions.assertUnchanged(before, VitalsSteps.getPatientObs(patientUUID).results(),
                "patient obs after POST /encounter with out-of-range value");
    }

    @Test
    @CreateEncounter(EncounterType.VITALS)
    public void adminCanDeleteVitalsEncounter() {
        var encounter = SessionStorage.getEncounter();
        VitalsSteps.assertPatientHasObsOf(patientUUID, encounter);

        new CrudRequester(
                RequestSpecs.adminSpec(),
                Endpoint.ENCOUNTER_DELETE,
                ResponseSpecs.requestReturnsNoContent()
        )
                .delete(encounter.getUuid());

        softly.assertThat(VitalsSteps.getPatientObs(patientUUID).results())
                .as("obs of deleted encounter are not returned for patient")
                .isEmpty();
        softly.assertThat(VitalsSteps.getEncounter(encounter.getUuid()).getVoided())
                .as("deleted encounter is marked as voided")
                .isTrue();
    }

    @Test
    @CreateEncounter(EncounterType.VITALS)
    public void adminCannotDeleteNonExistentEncounter() {
        var encounter = SessionStorage.getEncounter();
        VitalsSteps.assertPatientHasObsOf(patientUUID, encounter);
        var before = VitalsSteps.getPatientObs(patientUUID).results();

        new CrudRequester(
                RequestSpecs.adminSpec(),
                Endpoint.ENCOUNTER_DELETE,
                ResponseSpecs.requestReturnsNotFound()
        )
                .delete(UUID.randomUUID().toString());

        ModelAssertions.assertUnchanged(before, VitalsSteps.getPatientObs(patientUUID).results(),
                "patient obs after delete of non-existent encounter");
    }

    @Test
    @CreateEncounter(EncounterType.VITALS)
    public void unauthorizedUserCannotDeleteEncounter() {
        var encounter = SessionStorage.getEncounter();
        VitalsSteps.assertPatientHasObsOf(patientUUID, encounter);
        var obsBefore = VitalsSteps.getPatientObs(patientUUID).results();
        var encounterBefore = VitalsSteps.getEncounter(encounter.getUuid());

        new CrudRequester(
                RequestSpecs.unAuthSpec(),
                Endpoint.ENCOUNTER_DELETE,
                ResponseSpecs.requestReturnsUnauthorized()
        )
                .delete(encounter.getUuid());

        ModelAssertions.assertUnchanged(obsBefore, VitalsSteps.getPatientObs(patientUUID).results(),
                "patient obs after unauthorized delete");
        ModelAssertions.assertUnchanged(encounterBefore, VitalsSteps.getEncounter(encounter.getUuid()),
                "encounter after unauthorized delete (not voided)");
    }

    @Test
    @CreateEncounter(EncounterType.VITALS)
    public void adminCanDeleteSingleObs() {
        var encounter = SessionStorage.getEncounter();
        VitalsSteps.assertPatientHasObsOf(patientUUID, encounter);

        String deletedObsUUID = encounter.getObs().get(0).getUuid();

        new CrudRequester(
                RequestSpecs.adminSpec(),
                Endpoint.OBS_DELETE,
                ResponseSpecs.requestReturnsNoContent()
        )
                .delete(deletedObsUUID);

        Set<String> expectedObsUUIDs = new TreeSet<>(Uuids.of(encounter.getObs()));
        expectedObsUUIDs.remove(deletedObsUUID);

        softly.assertThat(Uuids.of(VitalsSteps.getPatientObs(patientUUID).results()))
                .as("only deleted obs is gone, other obs remain")
                .isEqualTo(expectedObsUUIDs);
    }
}
