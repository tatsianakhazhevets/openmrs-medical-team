package apiTests;

import apiParts.models.auth.LoginAdminRequest;
import apiParts.models.auth.LoginAdminResponse;
import apiParts.models.patient.*;
import apiParts.skelethon.endpoints.Endpoint;
import apiParts.skelethon.requests.auth.SuccessfulAuthRequester;
import apiParts.skelethon.requests.common.SuccessfulCrudRequester;
import apiParts.specs.RequestSpecs;
import apiParts.specs.ResponseSpecs;
import org.junit.jupiter.api.Test;

import java.util.List;


public class PatientManagementApiTests extends BaseTest {

    @Test
    public void adminCanCreatePatient() {

        LoginAdminRequest loginAdminRequest = LoginAdminRequest.builder()
                .username("admin")
                .password("Admin123")
                .build();

        new SuccessfulAuthRequester<LoginAdminResponse>(
                RequestSpecs.unAuthSpec(),
                Endpoint.LOGIN_GET,
                ResponseSpecs.requestReturnsOk())
                .login(loginAdminRequest);

        CreatePatientRequest createPatientRequest = CreatePatientRequest.builder()
                .person(PersonRequest.builder()
                        .gender("F")
                        .birthdate("1990-01-01")
                        .birthdateEstimated(false)
                        .dead(false)
                        .names(List.of(
                                PersonName.builder()
                                        .givenName("Olga")
                                        .familyName("Smitt")
                                        .build()))
                        .addresses(List.of(
                                PersonAddress.builder()
                                        .address1("15 Main Street")
                                        .cityVillage("Valencia")
                                        .country("Spain")
                                        .postalCode("46001")
                                        .build()))
                        .build())
                .identifiers(List.of(
                        PatientIdentifierRequest.builder()
                                .identifier("GVUMLE")
                                .identifierType("05a29f94-c0ed-11e2-94be-8c13b969e334")
                                .location("1ce1b7d4-c865-4178-82b0-5932e51503d6")
                                .preferred(true)
                                .build()))
                .build();

        new SuccessfulCrudRequester<CreatePatientRequest>(
                RequestSpecs.adminSpec(),
                Endpoint.PATIENT_POST,
                ResponseSpecs.requestReturnsCreated())
                .create(createPatientRequest);
    }
}