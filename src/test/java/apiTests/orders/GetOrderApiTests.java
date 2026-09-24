package apiTests.orders;

import apiParts.assertions.ModelAssertions;
import apiParts.models.EncounterType;
import apiParts.models.Location;
import apiParts.models.encounter.CreateEncounterRequest;
import apiParts.models.encounter.CreateEncounterResponse;
import apiParts.models.encounter.Ref;
import apiParts.models.order.CareSetting;
import apiParts.models.order.OrderSearchParams;
import apiParts.skelethon.endpoints.Endpoint;
import apiParts.skelethon.requests.crud.SuccessfulCrudRequester;
import apiParts.skelethon.requests.search.SearchRequester;
import apiParts.specs.RequestSpecs;
import apiParts.specs.ResponseSpecs;
import apiParts.steps.AdminSteps;
import apiTests.BaseTest;
import common.annotations.CreateOrder;
import common.annotations.CreatePatient;
import common.storages.SessionStorage;
import org.junit.jupiter.api.Test;

import java.util.List;

import static common.annotations.CreateOrder.Type.DRUG;

@CreatePatient
public class GetOrderApiTests extends BaseTest {

    @Test
    @CreateOrder(DRUG)
    public void adminCanFetchListOfOrders() {
        String patientUUID = SessionStorage.getPatient().getUuid();
        CreateEncounterRequest expectedRequest = AdminSteps.drugOrderEncounterRequest(patientUUID);
        var orders = AdminSteps.fetchDrugOrders(patientUUID);

        softly.assertThat(orders.results())
                .as("one created drug order returned")
                .hasSize(1);
        ModelAssertions.assertThatModels(softly, expectedRequest.getOrders(), orders.results())
                .as("drug orders returned for patient")
                .match();
    }

    @Test
    public void authRequiredToFetchListOfOrders() {
        OrderSearchParams searchParams = OrderSearchParams.builder()
                .patient(SessionStorage.getPatient().getUuid())
                .careSetting(CareSetting.INPATIENT.name())
                .limit(1)
                .representation("default")
                .build();

        new SearchRequester(
                RequestSpecs.unAuthSpec(),
                Endpoint.LIST_ORDERS_GET,
                ResponseSpecs.requestReturnsUnauthorized())
                .search(searchParams);
    }

    // Precondition: patient with a standard drug order encounter
    @Test
    @CreateOrder(DRUG)
    public void adminCanCheckSpecificOrderDetails() {
        String patientUUID = SessionStorage.getPatient().getUuid();
        CreateEncounterRequest drugOrderRequest = AdminSteps.drugOrderEncounterRequest(patientUUID);
        String orderUUID = SessionStorage.getOrderUuid();

        var drugOrders = AdminSteps.fetchDrugOrders(patientUUID).results();
        softly.assertThat(drugOrders)
                .as("created drug order is retrievable")
                .hasSize(1);
        var savedOrder = drugOrders.get(0);
        softly.assertThat(savedOrder.getUuid())
                .as("created order uuid")
                .isEqualTo(orderUUID);

        ModelAssertions.assertThatModels(softly, drugOrderRequest.getOrders(), List.of(savedOrder))
                .as("saved drug order")
                .match();

        // add a lab order (Alkaline phosphatase test) encounter for the same patient
        CreateEncounterRequest labOrderRequest = AdminSteps.labOrderEncounterRequest(patientUUID);
        var labEncounter = new SuccessfulCrudRequester<CreateEncounterResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.ENCOUNTER_POST,
                ResponseSpecs.requestReturnsCreated())
                .create(labOrderRequest);

        CreateEncounterResponse expectedLabEncounter = new CreateEncounterResponse();
        expectedLabEncounter.setPatient(Ref.of(patientUUID));
        expectedLabEncounter.setLocation(Ref.of(Location.INPATIENT_WARD.getUuid()));
        expectedLabEncounter.setEncounterType(Ref.of(EncounterType.ORDER.getUuid()));

        ModelAssertions.assertMatchesExpected(softly, labEncounter, expectedLabEncounter, "lab order encounter");

        softly.assertThat(labEncounter.getObs())
                .as("lab encounter obs")
                .isEmpty();
        softly.assertThat(labEncounter.getVoided())
                .as("lab encounter voided")
                .isFalse();
        softly.assertThat(labEncounter.getOrders())
                .as("lab encounter orders")
                .hasSize(1);
        softly.assertThat(labEncounter.getOrders().get(0).getDisplay())
                .as("lab order display")
                .isEqualTo("Alkaline phosphatase");
    }

}
