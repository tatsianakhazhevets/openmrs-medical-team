package apiTests.Visits;

import apiParts.models.Attribute;
import apiParts.models.VisitAttributeType;
import apiParts.models.VisitLocation;
import apiParts.models.VisitType;
import apiParts.models.encounter.Ref;
import apiParts.models.visit.CreateVisitRequest;
import apiParts.models.visit.CreateVisitResponse;
import apiParts.models.visit.GetVisitByUuidResponse;
import apiParts.skelethon.endpoints.Endpoint;
import apiParts.skelethon.requests.common.SuccessfulCrudRequester;
import apiParts.specs.RequestSpecs;
import apiParts.specs.ResponseSpecs;
import apiParts.steps.AdminSteps;
import apiTests.BaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

public class VisitsTests extends BaseTest {
    private String patientUUID;
    private String encounterUUID;

    @BeforeEach
    public void setUp() {
        var response = AdminSteps.createPatient();
        patientUUID = response.getUuid();
        var encounter = AdminSteps.createVitalsEncounter(patientUUID);
        encounterUUID = encounter.getUuid();
    }

    private CreateVisitRequest.CreateVisitRequestBuilder createVisit(String patientUUID, VisitType visitType) {
        return CreateVisitRequest.builder()
                .patient(patientUUID)
                .visitType(visitType);
    }

    private CreateVisitResponse createVisit(CreateVisitRequest request) {
        return new SuccessfulCrudRequester<CreateVisitResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.VISIT_POST,
                ResponseSpecs.requestReturnsCreated()).create(request);
    }

    private GetVisitByUuidResponse getVisit(String visitUUID) {
        return new SuccessfulCrudRequester<GetVisitByUuidResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.VISIT_GET,
                ResponseSpecs.requestReturnsOk())
                .get(visitUUID);
    }


    @Test
    public void AdminCanCreateVisitOnlyWithRequiredFields() {
        var visit = AdminSteps.createVisitWithRequiredFields(patientUUID);

        softly.assertThat(visit.getUuid()).as("visit uuid").isNotBlank();
        softly.assertThat(visit.getPatient().getUuid()).as("patient uuid").isEqualTo(patientUUID);
        softly.assertThat(visit.getVisitType().getUuid()).as("visit type uuid").isEqualTo(VisitType.FACILITY_VISIT.getUuid());
        softly.assertThat(visit.getLocation()).as("location").isNull();
        softly.assertThat(visit.getStopDatetime()).as("stop datetime").isNull();
        softly.assertThat(visit.getStartDatetime()).as("start datetime").isNotNull();
        softly.assertThat(visit.getEncounters()).as("encounters").isEmpty();
        softly.assertThat(visit.getAttributes()).as("attributes").isEmpty();
    }

    @Test
    public void AdminCanCreateVisitWithOptionalFields() {
        var request = createVisit(patientUUID, VisitType.FACILITY_VISIT)
                .location(VisitLocation.UBUNTU_HOSPITAL)
                .encounters(List.of(encounterUUID))
                .attributes(List.of(
                        Attribute.builder()
                                .attributeType(VisitAttributeType.INSURANCE_POLICY_NUMBER)
                                .value("POLICY-12345")
                                .build()
                ))
                .build();
        var visit = createVisit(request);
        softly.assertThat(visit.getUuid()).as("create visit uuid").isNotBlank();

        var savedVisit = getVisit(visit.getUuid());

        softly.assertThat(savedVisit.getUuid()).as("visit uuid").isEqualTo(visit.getUuid());
        softly.assertThat(savedVisit.getPatient().getUuid()).as("patient uuid").isEqualTo(patientUUID);
        softly.assertThat(savedVisit.getVisitType().getUuid()).as("visit type uuid").isEqualTo(VisitType.FACILITY_VISIT.getUuid());
        softly.assertThat(savedVisit.getLocation().getUuid()).as("location uuid").isEqualTo(VisitLocation.UBUNTU_HOSPITAL.getUuid());
        softly.assertThat(savedVisit.getAttributes()).extracting(Ref::getDisplay).containsExactly("Insurance Policy Number: POLICY-12345");
        softly.assertThat(savedVisit.getStopDatetime()).as("stop datetime").isNull();
        softly.assertThat(savedVisit.getStartDatetime()).as("start datetime").isNotNull();
    }

    @Test
    public void UnauthorizedUserCannotCreateVisit() {
        var request = createVisit(patientUUID, VisitType.FACILITY_VISIT).build();

        new SuccessfulCrudRequester<CreateVisitResponse>(
                RequestSpecs.unAuthSpec(),
                Endpoint.VISIT_POST,
                ResponseSpecs.requestReturnsBadRequestWithMessage("Privileges required: Get Patients"))
                .create(request);
    }

    @Test
    public void CannotCreateVisitWithoutPatient() {
        var request = CreateVisitRequest.builder()
                .visitType(VisitType.FACILITY_VISIT)
                .build();

        new SuccessfulCrudRequester<CreateVisitResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.VISIT_POST,
                ResponseSpecs.requestReturnsBadRequestWithMessage("Some required properties are missing: patient"))
                .create(request);
    }

    @Test
    public void AdminCanUpdateVisit() {
        var request = createVisit(patientUUID, VisitType.FACILITY_VISIT)
                .location(VisitLocation.UBUNTU_HOSPITAL)
                .encounters(List.of(encounterUUID))
                .attributes(List.of(
                        Attribute.builder()
                                .attributeType(VisitAttributeType.INSURANCE_POLICY_NUMBER)
                                .value("POLICY-12345")
                                .build()
                ))
                .build();

        var visit = createVisit(request);

        var updatedRequest = CreateVisitRequest.builder()
                .visitType(VisitType.HOME_VISIT)
                .location(VisitLocation.MOBILE_CLINIC)
                .build();

        new SuccessfulCrudRequester<CreateVisitResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.VISIT_POST,
                ResponseSpecs.requestReturnsOk()).update(visit.getUuid(), updatedRequest);

        var updatedVisit = getVisit(visit.getUuid());

        softly.assertThat(updatedVisit.getUuid()).as("visit uuid").isEqualTo(visit.getUuid());
        softly.assertThat(updatedVisit.getPatient().getUuid()).as("patient uuid").isEqualTo(patientUUID);
        softly.assertThat(updatedVisit.getVisitType().getUuid()).as("updated visit type").isEqualTo(VisitType.HOME_VISIT.getUuid());
        softly.assertThat(updatedVisit.getLocation().getUuid()).as("updated location").isEqualTo(VisitLocation.MOBILE_CLINIC.getUuid());
    }

    @Test
    public void UpdateNonExistentVisitReturnsNotFound() {
        var nonExistentVisitUUID = "82f18b44-6814-11e8-923f-e9a88dcb533f";

        var updatedRequest = CreateVisitRequest.builder()
                .visitType(VisitType.HOME_VISIT)
                .location(VisitLocation.MOBILE_CLINIC)
                .build();

        new SuccessfulCrudRequester<CreateVisitResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.VISIT_POST,
                ResponseSpecs.requestReturnsNotFound())
                .update(nonExistentVisitUUID, updatedRequest);
    }

    @Test
    public void UnauthorizedUserCannotUpdateVisit() {
        var request = createVisit(patientUUID, VisitType.FACILITY_VISIT)
                .location(VisitLocation.UBUNTU_HOSPITAL)
                .build();

        var visit = createVisit(request);

        var updatedRequest = CreateVisitRequest.builder()
                .visitType(VisitType.HOME_VISIT)
                .build();

        new SuccessfulCrudRequester<CreateVisitResponse>(
                RequestSpecs.unAuthSpec(),
                Endpoint.VISIT_POST,
                ResponseSpecs.requestReturnsUnauthorized())
                .update(visit.getUuid(), updatedRequest);
    }

    @Test
    public void AdminCanRetireVisit() {
        var request = createVisit(patientUUID, VisitType.FACILITY_VISIT)
                .location(VisitLocation.UBUNTU_HOSPITAL)
                .encounters(List.of(encounterUUID))
                .attributes(List.of(
                        Attribute.builder()
                                .attributeType(VisitAttributeType.INSURANCE_POLICY_NUMBER)
                                .value("POLICY-12345")
                                .build()
                ))
                .build();

        var visit = createVisit(request);

        new SuccessfulCrudRequester<CreateVisitResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.VISIT_DELETE,
                ResponseSpecs.requestReturnsNoContent()).delete(visit.getUuid());

        var retiredVisit = getVisit(visit.getUuid());

        softly.assertThat(retiredVisit.getUuid()).as("visit uuid").isEqualTo(visit.getUuid());
        softly.assertThat(retiredVisit.getVoided()).as("visit should be retired").isTrue();
    }

    @Test
    public void AdminCanPurgeVisit() {
        var request = createVisit(patientUUID, VisitType.FACILITY_VISIT)
                .location(VisitLocation.UBUNTU_HOSPITAL)
                .encounters(List.of(encounterUUID))
                .attributes(List.of(
                        Attribute.builder()
                                .attributeType(VisitAttributeType.INSURANCE_POLICY_NUMBER)
                                .value("POLICY-12345")
                                .build()
                ))
                .build();

        var visit = createVisit(request);

        new SuccessfulCrudRequester<CreateVisitResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.VISIT_DELETE,
                ResponseSpecs.requestReturnsNoContent()).delete(visit.getUuid(), Map.of("purge", true));

        new SuccessfulCrudRequester<GetVisitByUuidResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.VISIT_GET,
                ResponseSpecs.requestReturnsNotFound())
                .get(visit.getUuid());
    }

    @Test
    public void DeleteNonExistentVisitReturnsNotFound() {
        var nonExistentVisitUUID = "82f18b44-6814-11e8-923f-e9a88dcb533f";

        new SuccessfulCrudRequester<CreateVisitResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.VISIT_DELETE,
                ResponseSpecs.requestReturnsNotFound())
                .delete(nonExistentVisitUUID);
    }

    @Test
    public void UnauthorizedUserCannotDeleteVisit() {
        var request = createVisit(patientUUID, VisitType.FACILITY_VISIT)
                .location(VisitLocation.UBUNTU_HOSPITAL)
                .build();

        var visit = createVisit(request);

        new SuccessfulCrudRequester<CreateVisitResponse>(
                RequestSpecs.unAuthSpec(),
                Endpoint.VISIT_DELETE,
                ResponseSpecs.requestReturnsUnauthorized())
                .delete(visit.getUuid());
    }
}
