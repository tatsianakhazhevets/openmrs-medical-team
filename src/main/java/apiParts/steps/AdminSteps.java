package apiParts.steps;

import apiParts.models.EncounterType;
import apiParts.models.Location;
import apiParts.models.auth.LoginAdminRequest;
import apiParts.models.auth.LoginAdminResponse;
import apiParts.models.encounter.CreateEncounterRequest;
import apiParts.models.encounter.CreateEncounterRequest.Obs;
import apiParts.models.encounter.CreateEncounterResponse;
import apiParts.models.patient.*;
import apiParts.skelethon.endpoints.Endpoint;
import apiParts.skelethon.requests.auth.SuccessfulAuthRequester;
import apiParts.skelethon.requests.common.SuccessfulCrudRequester;
import apiParts.specs.RequestSpecs;
import apiParts.specs.ResponseSpecs;
import net.datafaker.Faker;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Locale;

import static apiParts.models.VitalsConcept.*;

public class AdminSteps {

    static Faker faker = new Faker(new Locale("en", "US"));

    public static CreatePatientResponse createPatient() {
        LoginAdminRequest loginAdminRequest = LoginAdminRequest.builder()
                .username("admin")
                .password("Admin123")
                .build();

        new SuccessfulAuthRequester<LoginAdminResponse>(
                RequestSpecs.unAuthSpec(),
                Endpoint.LOGIN_GET,
                ResponseSpecs.requestReturnsOk())
                .login(loginAdminRequest);

        String gender = faker.gender().binaryTypes(); // "Male" / "Female"
        String shortGender = gender.equals("Male") ? "M" : "F";

        CreatePatientRequest createPatientRequest = CreatePatientRequest.builder()
                .person(PersonRequest.builder()
                        .gender(shortGender)
                        .birthdate(faker.timeAndDate().birthday(18, 65, "yyyy-MM-dd"))
                        .birthdateEstimated(false)
                        .dead(false)
                        .names(List.of(
                                PersonName.builder()
                                        .givenName(faker.name().firstName())
                                        .familyName(faker.name().lastName())
                                        .build()))
                        .addresses(List.of(
                                PersonAddress.builder()
                                        .address1(faker.address().streetAddress())
                                        .cityVillage(faker.address().city())
                                        .country(StringUtils.left(faker.address().country(), 50))  //was flaky because of "country": "British Indian Ocean Territory (Chagos Archipelago)"
                                        .postalCode(faker.address().postcode())
                                        .build()))
                        .build())
                .identifiers(List.of(
                        PatientIdentifierRequest.builder()
                                .identifier(getId())
                                .identifierType("05a29f94-c0ed-11e2-94be-8c13b969e334") //MRS ID GET /openmrs/ws/rest/v1/patientidentifiertype?v=custom:(uuid,name,required,uniquenessBehavior,locationBehavior)
                                .location("dbdaabf6-a326-4804-aba7-062073e05cd1") //Outpatient Clinic DOTO - move to ENUM?
                                .preferred(true)
                                .build()))
                .build();

        return new SuccessfulCrudRequester<CreatePatientResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.PATIENT_POST,
                ResponseSpecs.requestReturnsCreated())
                .create(createPatientRequest);
    }

    public static CreateEncounterResponse createVitalsEncounter(String patientUUID) {
        CreateEncounterRequest createEncounterRequest = CreateEncounterRequest.builder()
                .patient(patientUUID)
                .encounterType(EncounterType.VITALS)
                .location(Location.OUTPATIENT_CLINIC)
                .obs(List.of(
                        Obs.of(SYSTOLIC_BP, 100),
                        Obs.of(DIASTOLIC_BP, 70),
                        Obs.of(RESPIRATORY_RATE, 14),
                        Obs.of(OXYGEN_SATURATION, 95),
                        Obs.of(PULSE, 68),
                        Obs.of(TEMPERATURE, 37),
                        Obs.of(GENERAL_NOTE, "Some note"),
                        Obs.of(WEIGHT, 90.2),
                        Obs.of(HEIGHT, 177.3),
                        Obs.of(MID_UPPER_ARM_CIRC, 14),
                        Obs.of(BMI, 28.7)))
                .build();

        return new SuccessfulCrudRequester<CreateEncounterResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.ENCOUNTER_POST,
                ResponseSpecs.requestReturnsCreated())
                .create(createEncounterRequest);
    }

    // Provider linked to admin user (GET /session -> currentProvider), used as order.orderer
    public static String getCurrentProviderUuid() {
        LoginAdminRequest loginAdminRequest = LoginAdminRequest.builder()
                .username("admin")
                .password("Admin123")
                .build();

        LoginAdminResponse session = new SuccessfulAuthRequester<LoginAdminResponse>(
                RequestSpecs.unAuthSpec(),
                Endpoint.LOGIN_GET,
                ResponseSpecs.requestReturnsOk())
                .login(loginAdminRequest);

        return session.getCurrentProvider().getUuid();
    }

    // ======== HELPERS ========
    private static String getId() {
        var response = new SuccessfulCrudRequester<GetIdentifierResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.IDENTIFIER_GET,
                ResponseSpecs.requestReturnsCreated())
                .create();

        return response.getIdentifier();
    }
}
