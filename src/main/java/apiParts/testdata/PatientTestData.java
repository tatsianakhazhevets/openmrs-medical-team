package apiParts.testdata;

import apiParts.models.patient.CreatePatientRequest;
import apiParts.models.patient.IdentifierLocation;
import apiParts.models.patient.IdentifierType;
import apiParts.models.patient.PatientIdentifierRequest;
import apiParts.models.patient.PersonAddress;
import apiParts.models.patient.PersonName;
import apiParts.models.patient.PersonRequest;
import net.datafaker.Faker;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Locale;

/**
 * Request of the standard patient fixture (see apiParts.steps.AdminSteps#createPatient).
 */
public class PatientTestData {

    private static final Faker faker = new Faker(new Locale("en", "US"));

    private static final int MIN_AGE = 18;
    private static final int MAX_AGE = 65;
    private static final String BIRTHDATE_PATTERN = "yyyy-MM-dd";
    // Server rejects longer values, e.g. "British Indian Ocean Territory (Chagos Archipelago)"
    private static final int MAX_COUNTRY_LENGTH = 50;

    private PatientTestData() {
    }

    // Adult patient with one preferred MRS identifier; identifier is issued by the server
    public static CreatePatientRequest createPatientRequest(String identifier) {
        String gender = faker.gender().binaryTypes(); // "Male" / "Female"
        String shortGender = gender.equals("Male") ? "M" : "F";

        return CreatePatientRequest.builder()
                .person(PersonRequest.builder()
                        .gender(shortGender)
                        .birthdate(faker.timeAndDate().birthday(MIN_AGE, MAX_AGE, BIRTHDATE_PATTERN))
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
                                        .country(StringUtils.left(faker.address().country(), MAX_COUNTRY_LENGTH))
                                        .postalCode(faker.address().postcode())
                                        .build()))
                        .build())
                .identifiers(List.of(
                        PatientIdentifierRequest.builder()
                                .identifier(identifier)
                                .identifierType(IdentifierType.MRS_ID.getUuid())
                                .location(IdentifierLocation.OUTPATIENT_CLINIC.getUuid())
                                .preferred(true)
                                .build()))
                .build();
    }
}
