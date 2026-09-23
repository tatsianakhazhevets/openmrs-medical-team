package apiTests.visits;

import apiParts.assertions.ModelAssertions;
import apiParts.models.Attribute;
import apiParts.models.EncounterType;
import apiParts.models.visit.VisitAttributeType;
import apiParts.models.visit.VisitLocation;
import apiParts.models.visit.VisitType;
import apiParts.models.encounter.Ref;
import apiParts.models.visit.CreateVisitRequest;
import apiParts.models.visit.CreateVisitResponse;
import apiParts.models.visit.GetVisitResponse;
import apiParts.skelethon.endpoints.Endpoint;
import apiParts.skelethon.requests.crud.CrudRequester;
import apiParts.skelethon.requests.crud.SuccessfulCrudRequester;
import apiParts.specs.RequestSpecs;
import apiParts.specs.ResponseSpecs;
import apiParts.steps.AdminSteps;
import apiTests.BaseTest;
import common.annotations.CreateEncounter;
import common.annotations.CreatePatient;
import common.storages.SessionStorage;
import net.datafaker.Faker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Locale;
import java.util.Map;

@CreatePatient
public class VisitsTests extends BaseTest {
    private static final Faker FAKER = new Faker(new Locale("en", "US"));
    private static final String NON_EXISTENT_VISIT_UUID =
            "82f18b44-6814-11e8-923f-e9a88dcb533f";
    private String patientUUID;
    private String visitUUID;
    private final String insurancePolicyNumber = FAKER.bothify("POLICY-#####");

    @BeforeEach
    public void setUp() {
        patientUUID = SessionStorage.getPatient().getUuid();

    }

    @AfterEach
    public void tearDown() {
        if (visitUUID != null) {
            AdminSteps.deleteVisit(visitUUID);
        }
    }

    private CreateVisitRequest.CreateVisitRequestBuilder createVisit(String patientUUID, VisitType visitType) {
        return CreateVisitRequest.builder()
                .patient(patientUUID)
                .visitType(visitType);
    }


    @Test
    public void adminCanCreateVisitOnlyWithRequiredFields() {
        CreateVisitRequest request = createVisit(patientUUID, VisitType.FACILITY_VISIT).build();
        CreateVisitResponse visit = AdminSteps.createVisit(request);
        visitUUID = visit.getUuid();
        softly.assertThat(visit.getUuid()).as("visit uuid").isNotBlank();
        ModelAssertions.assertThatModels(softly, request, visit).as("created visit").match();
        softly.assertThat(visit.getLocation()).as("location").isNull();
        softly.assertThat(visit.getStopDatetime()).as("stop datetime").isNull();
        softly.assertThat(visit.getStartDatetime()).as("start datetime").isNotNull();
        softly.assertThat(visit.getEncounters()).as("encounters").isEmpty();
        softly.assertThat(visit.getAttributes()).as("attributes").isEmpty();
        softly.assertAll();
    }

    @CreateEncounter(EncounterType.VITALS)
    @Test
    public void adminCanCreateVisitWithOptionalFields() {
        String encounterUUID = SessionStorage.getEncounter().getUuid();
        CreateVisitRequest request = createVisit(patientUUID, VisitType.FACILITY_VISIT)
                .location(VisitLocation.UBUNTU_HOSPITAL)
                .encounters(List.of(encounterUUID))
                .attributes(List.of(
                        Attribute.builder()
                                .attributeType(VisitAttributeType.INSURANCE_POLICY_NUMBER)
                                .value(insurancePolicyNumber)
                                .build()
                ))
                .build();
        CreateVisitResponse visit = AdminSteps.createVisit(request);
        visitUUID = visit.getUuid();
        softly.assertThat(visit.getUuid()).as("create visit uuid").isNotBlank();

        GetVisitResponse savedVisit = AdminSteps.getVisit(visitUUID);

        softly.assertThat(savedVisit.getUuid()).as("visit uuid").isEqualTo(visit.getUuid());
        ModelAssertions.assertThatModels(softly, request, savedVisit).as("created visit").match();
        softly.assertThat(savedVisit.getAttributes()).extracting(Ref::getDisplay).containsExactly(
                VisitAttributeType.INSURANCE_POLICY_NUMBER.getDisplay() + ": " + insurancePolicyNumber);
        softly.assertThat(savedVisit.getStopDatetime()).as("stop datetime").isNull();
        softly.assertThat(savedVisit.getStartDatetime()).as("start datetime").isNotNull();
        softly.assertAll();
    }

    @Test
    public void unauthorizedUserCannotCreateVisit() {
        CreateVisitRequest request = createVisit(patientUUID, VisitType.FACILITY_VISIT).build();


        new CrudRequester(
                RequestSpecs.unAuthSpec(),
                Endpoint.VISIT_POST,
                ResponseSpecs.requestReturnsBadRequestWithMessage("Privileges required: Get Patients"))
                .create(request);
    }

    @Test
    public void cannotCreateVisitWithoutPatient() {
        CreateVisitRequest request = CreateVisitRequest.builder()
                .visitType(VisitType.FACILITY_VISIT)
                .build();

        new CrudRequester(
                RequestSpecs.adminSpec(),
                Endpoint.VISIT_POST,
                ResponseSpecs.requestReturnsBadRequestWithMessage("Some required properties are missing: patient"))
                .create(request);
    }

    @CreateEncounter(EncounterType.VITALS)
    @Test
    public void adminCanUpdateVisit() {
        String encounterUUID = SessionStorage.getEncounter().getUuid();
        CreateVisitRequest request = createVisit(patientUUID, VisitType.FACILITY_VISIT)
                .location(VisitLocation.UBUNTU_HOSPITAL)
                .encounters(List.of(encounterUUID))
                .attributes(List.of(
                        Attribute.builder()
                                .attributeType(VisitAttributeType.INSURANCE_POLICY_NUMBER)
                                .value(insurancePolicyNumber)
                                .build()
                ))
                .build();

        CreateVisitResponse visit = AdminSteps.createVisit(request);
        visitUUID = visit.getUuid();

        CreateVisitRequest updatedRequest = CreateVisitRequest.builder()
                .visitType(VisitType.HOME_VISIT)
                .location(VisitLocation.MOBILE_CLINIC)
                .build();

        new SuccessfulCrudRequester<CreateVisitResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.VISIT_POST,
                ResponseSpecs.requestReturnsOk()).update(visit.getUuid(), updatedRequest);

        GetVisitResponse updatedVisit = AdminSteps.getVisit(visitUUID);

        softly.assertThat(updatedVisit.getUuid()).as("visit uuid").isEqualTo(visit.getUuid());
        softly.assertThat(updatedVisit.getPatient().getUuid()).as("patient uuid").isEqualTo(patientUUID);
        ModelAssertions.assertThatModels(softly, updatedRequest, updatedVisit).as("updated visit").match();
        softly.assertAll();
    }

    @Test
    public void updateNonExistentVisitReturnsNotFound() {
        CreateVisitRequest updatedRequest = CreateVisitRequest.builder()
                .visitType(VisitType.HOME_VISIT)
                .location(VisitLocation.MOBILE_CLINIC)
                .build();

        new CrudRequester(
                RequestSpecs.adminSpec(),
                Endpoint.VISIT_POST,
                ResponseSpecs.requestReturnsNotFound())
                .update(NON_EXISTENT_VISIT_UUID, updatedRequest);
    }

    @Test
    public void unauthorizedUserCannotUpdateVisit() {
        CreateVisitRequest request = createVisit(patientUUID, VisitType.FACILITY_VISIT)
                .location(VisitLocation.UBUNTU_HOSPITAL)
                .build();

        CreateVisitResponse visit = AdminSteps.createVisit(request);
        visitUUID = visit.getUuid();

        CreateVisitRequest updatedRequest = CreateVisitRequest.builder()
                .visitType(VisitType.HOME_VISIT)
                .build();

        new CrudRequester(
                RequestSpecs.unAuthSpec(),
                Endpoint.VISIT_POST,
                ResponseSpecs.requestReturnsUnauthorized())
                .update(visit.getUuid(), updatedRequest);
    }

    @CreateEncounter(EncounterType.VITALS)
    @Test
    public void adminCanRetireVisit() {
        String encounterUUID = SessionStorage.getEncounter().getUuid();
        CreateVisitRequest request = createVisit(patientUUID, VisitType.FACILITY_VISIT)
                .location(VisitLocation.UBUNTU_HOSPITAL)
                .encounters(List.of(encounterUUID))
                .attributes(List.of(
                        Attribute.builder()
                                .attributeType(VisitAttributeType.INSURANCE_POLICY_NUMBER)
                                .value(insurancePolicyNumber)
                                .build()
                ))
                .build();

        CreateVisitResponse visit = AdminSteps.createVisit(request);

        new SuccessfulCrudRequester<CreateVisitResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.VISIT_DELETE,
                ResponseSpecs.requestReturnsNoContent()).delete(visit.getUuid());

        GetVisitResponse retiredVisit = AdminSteps.getVisit(visit.getUuid());

        softly.assertThat(retiredVisit.getUuid()).as("visit uuid").isEqualTo(visit.getUuid());
        softly.assertThat(retiredVisit.getVoided()).as("visit should be retired").isTrue();
        softly.assertAll();
    }

    @CreateEncounter(EncounterType.VITALS)
    @Test
    public void adminCanPurgeVisit() {
        String encounterUUID = SessionStorage.getEncounter().getUuid();
        CreateVisitRequest request = createVisit(patientUUID, VisitType.FACILITY_VISIT)
                .location(VisitLocation.UBUNTU_HOSPITAL)
                .encounters(List.of(encounterUUID))
                .attributes(List.of(
                        Attribute.builder()
                                .attributeType(VisitAttributeType.INSURANCE_POLICY_NUMBER)
                                .value(insurancePolicyNumber)
                                .build()
                ))
                .build();

        CreateVisitResponse visit = AdminSteps.createVisit(request);

        new SuccessfulCrudRequester<CreateVisitResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.VISIT_DELETE,
                ResponseSpecs.requestReturnsNoContent()).delete(visit.getUuid(), Map.of("purge", true));
        new CrudRequester(
                RequestSpecs.adminSpec(),
                Endpoint.VISIT_GET,
                ResponseSpecs.requestReturnsNotFound())
                .get(visit.getUuid());
    }

    @Test
    public void deleteNonExistentVisitReturnsNotFound() {
        new CrudRequester(
                RequestSpecs.adminSpec(),
                Endpoint.VISIT_DELETE,
                ResponseSpecs.requestReturnsNotFound())
                .delete(NON_EXISTENT_VISIT_UUID);
    }

    @Test
    public void unauthorizedUserCannotDeleteVisit() {
        CreateVisitRequest request = createVisit(patientUUID, VisitType.FACILITY_VISIT)
                .location(VisitLocation.UBUNTU_HOSPITAL)
                .build();

        CreateVisitResponse visit = AdminSteps.createVisit(request);
        visitUUID = visit.getUuid();

        new CrudRequester(
                RequestSpecs.unAuthSpec(),
                Endpoint.VISIT_DELETE,
                ResponseSpecs.requestReturnsUnauthorized())
                .delete(visit.getUuid());
    }
}
