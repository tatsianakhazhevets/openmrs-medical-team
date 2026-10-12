package apiParts.testdata;

import apiParts.generators.RandomModelGenerator;
import apiParts.models.patient.CreatePatientRequest;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Request of the standard patient fixture (see apiParts.steps.AdminSteps#createPatient).
 */
public class PatientTestData {
    private static final int ADULT_MIN_AGE = 20;
    private static final int ADULT_MAX_AGE = 80;
    // same format as GenerationStrategy.DATE in RandomModelGenerator
    private static final DateTimeFormatter BIRTHDATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    private PatientTestData() {
    }

    public static CreatePatientRequest createPatientRequest(String identifier) {
        CreatePatientRequest request = RandomModelGenerator.generate(CreatePatientRequest.class);
        request.getPerson().setAddresses(List.of());
        return request;
    }

    // gender: "M" / "F" / "UNKNOWN", empty - keep generated; adult: birthdate 20..80 years ago
    public static CreatePatientRequest createPatientRequest(String identifier, String gender, boolean adult) {
        CreatePatientRequest request = createPatientRequest(identifier);
        if (!gender.isBlank()) {
            request.getPerson().setGender(gender);
        }
        if (adult) {
            request.getPerson().setBirthdate(adultBirthdate());
        }
        return request;
    }

    private static String adultBirthdate() {
        return LocalDate.now()
                .minusYears(RandomModelGenerator.randomInt(ADULT_MIN_AGE, ADULT_MAX_AGE))
                .minusDays(RandomModelGenerator.randomInt(0, 364))
                .atStartOfDay()
                .atOffset(ZoneOffset.UTC)
                .format(BIRTHDATE_FORMAT);
    }

    public static CreatePatientRequest createPatientRequest() {
        CreatePatientRequest request = RandomModelGenerator.generate(CreatePatientRequest.class);
        request.getPerson().setAddresses(List.of());
        return request;
    }
}