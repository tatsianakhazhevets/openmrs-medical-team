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

    @Test
    public void AdminCanCreateVisitOnlyWithRequiredFields() {
        var request = CreateVisitRequest.builder().patient(patientUUID)
                .visitType(VisitType.FACILITY_VISIT)
                .build();

        var visit = new SuccessfulCrudRequester<CreateVisitResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.VISIT_POST,
                ResponseSpecs.requestReturnsCreated()).create(request);
        softly.assertThat(visit.getUuid()).as("create visit uuid").isNotBlank();
        softly.assertThat(visit.getUuid())
                .as("visit uuid")
                .isNotBlank();

        softly.assertThat(visit.getPatient().getUuid())
                .as("patient uuid")
                .isEqualTo(patientUUID);

        softly.assertThat(visit.getVisitType().getUuid())
                .as("visit type uuid")
                .isEqualTo(VisitType.FACILITY_VISIT.getUuid());

        softly.assertThat(visit.getLocation())
                .as("location")
                .isNull();

        softly.assertThat(visit.getStopDatetime())
                .as("stop datetime")
                .isNull();

        softly.assertThat(visit.getStartDatetime())
                .as("start datetime")
                .isNotNull();

        softly.assertThat(visit.getEncounters())
                .as("encounters")
                .isEmpty();

        softly.assertThat(visit.getAttributes())
                .as("attributes")
                .isEmpty();
    }

    @Test
    public void AdminCanCreateVisitOnlyWithAllFields() {


        var request = CreateVisitRequest.builder().patient(patientUUID)
                .visitType(VisitType.FACILITY_VISIT)
                .location(VisitLocation.UBUNTU_HOSPITAL)
                .encounters(List.of(encounterUUID))
                .attributes(List.of(
                        Attribute.builder()
                                .attributeType(VisitAttributeType.INSURANCE_POLICY_NUMBER)
                                .value("POLICY-12345")
                                .build()
                ))
                .build();

        var visit = new SuccessfulCrudRequester<CreateVisitResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.VISIT_POST,
                ResponseSpecs.requestReturnsCreated()).create(request);
        softly.assertThat(visit.getUuid()).as("create visit uuid").isNotBlank();

        var savedVisit = new SuccessfulCrudRequester<GetVisitByUuidResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.VISIT_GET,
                ResponseSpecs.requestReturnsOk())
                .get(visit.getUuid());

        softly.assertThat(savedVisit.getUuid())
                .as("visit uuid")
                .isEqualTo(visit.getUuid());

        softly.assertThat(savedVisit.getPatient().getUuid())
                .as("patient uuid")
                .isEqualTo(patientUUID);

        softly.assertThat(savedVisit.getVisitType().getUuid())
                .as("visit type uuid")
                .isEqualTo(VisitType.FACILITY_VISIT.getUuid());

        softly.assertThat(savedVisit.getLocation().getUuid())
                .as("location uuid")
                .isEqualTo(VisitLocation.UBUNTU_HOSPITAL.getUuid());

        softly.assertThat(savedVisit.getAttributes())
                .extracting(Ref::getDisplay)
                .containsExactly("Insurance Policy Number: POLICY-12345");

        softly.assertThat(savedVisit.getStopDatetime())
                .as("stop datetime")
                .isNull();

        softly.assertThat(savedVisit.getStartDatetime())
                .as("start datetime")
                .isNotNull();
    }
}
