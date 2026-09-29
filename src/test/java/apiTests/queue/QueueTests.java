package apiTests.queue;

import apiParts.assertions.ModelAssertions;
import apiParts.generators.RandomModelGenerator;
import apiParts.models.encounter.Ref;
import apiParts.models.queue.*;
import apiParts.models.queueEntry.*;
import apiParts.skelethon.endpoints.Endpoint;
import apiParts.models.search.SearchResult;
import apiParts.skelethon.requests.crud.CrudRequester;
import apiParts.specs.RequestSpecs;
import apiParts.specs.ResponseSpecs;
import apiParts.steps.AdminSteps;
import apiTests.BaseTest;
import common.annotations.CreatePatient;
import common.annotations.CreateVisit;
import common.storages.SessionStorage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


@CreateVisit
@CreatePatient
public class QueueTests extends BaseTest {
    private static final String NON_EXISTING_QUEUE_ENTRY_UUID =
            "00000000-0000-0000-0000-000000000000";
    private String patientUUID;
    private String visitUUID;
    private String queueEntryUUID;

    @BeforeEach
    public void setUp() {
        patientUUID = SessionStorage.getPatient().getUuid();
        visitUUID = SessionStorage.getVisit().getUuid();
    }

    @AfterEach
    void tearDown() {
        if (queueEntryUUID != null) {
            AdminSteps.endQueueEntry(queueEntryUUID);
        }
    }

    @Test
    void shouldAddPatientToQueue() {
        QueueEntryResponse response =
                AdminSteps.addPatientToQueue(patientUUID, visitUUID);

        queueEntryUUID = response.getUuid();

        QueueEntryResponse foundEntry = AdminSteps.getActiveQueueEntries().requireOne(
                entry -> entry.getUuid().equals(response.getUuid()),
                "created queue entry " + response.getUuid());
        ModelAssertions.assertMatchesExpected(softly, foundEntry, response, "queue entry");
    }

    @Test
    void shouldEndQueueEntry() {
        QueueEntryResponse response =
                AdminSteps.addPatientToQueue(patientUUID, visitUUID);

        EndQueueEntryRequest endRequest =
                RandomModelGenerator.generate(EndQueueEntryRequest.class);
        QueueEntryResponse endedResponse =
                AdminSteps.endQueueEntry(response.getUuid(), endRequest);

        softly.assertThat(endedResponse.getUuid()).isEqualTo(response.getUuid());
        softly.assertThat(endedResponse.getEndedAt()).isNotNull();
        SearchResult<QueueEntryResponse> activeEntries = AdminSteps.getActiveQueueEntries();
        softly.assertThat(activeEntries.results()).noneMatch(entry -> entry.getUuid().equals(response.getUuid()));
    }

    @Test
    void shouldUpdateQueueEntry() {
        QueuePriority priority = QueuePriority.URGENT;
        QueueStatus status = QueueStatus.FINISHED_SERVICE;

        QueueEntryResponse createdResponse =
                AdminSteps.addPatientToQueue(patientUUID, visitUUID);
        queueEntryUUID = createdResponse.getUuid();

        UpdateQueueEntryRequest updateRequest =
                RandomModelGenerator.generate(UpdateQueueEntryRequest.class);
        updateRequest.setStatus(status.toRef());
        updateRequest.setPriority(priority.toRef());

        AdminSteps.updateQueueEntry(createdResponse.getUuid(), updateRequest);

        QueueEntryResponse expected = new QueueEntryResponse();
        expected.setStatus(status.toRef());
        expected.setPriority(priority.toRef());

        QueueEntryResponse foundEntry = AdminSteps.getActiveQueueEntries().requireOne(
                entry -> entry.getUuid().equals(createdResponse.getUuid()),
                "updated queue entry " + createdResponse.getUuid());

        ModelAssertions.assertMatchesExpected(softly, foundEntry, expected, "updated queue entry");
    }

    @Test
    void shouldNotAddPatientToQueueWithoutPatient() {
        QueueResponse queue = AdminSteps.getOutpatientConsultationQueue();

        CreateQueueEntryRequest request =
                RandomModelGenerator.generate(CreateQueueEntryRequest.class);
        request.setVisit(Ref.of(visitUUID));

        CreateQueueEntryRequest.QueueEntry queueEntry =
                RandomModelGenerator.generate(CreateQueueEntryRequest.QueueEntry.class);
        queueEntry.setStatus(QueueStatus.WAITING.toRef());
        queueEntry.setPriority(QueuePriority.NOT_URGENT.toRef());
        queueEntry.setQueue(Ref.of(queue.getUuid()));
        queueEntry.setPatient(null);

        request.setQueueEntry(queueEntry);

        new CrudRequester(
                RequestSpecs.adminSpec(),
                Endpoint.VISIT_QUEUE_ENTRY_POST,
                ResponseSpecs.requestReturnsInvalidSubmission("patient")
        ).create(request);

        softly.assertThat(AdminSteps.getActiveQueueEntries().results())
                .noneMatch(entry -> entry.getVisit() != null && visitUUID.equals(entry.getVisit().getUuid()));
    }

    @Test
    void shouldNotUpdateNonExistingQueueEntry() {
        UpdateQueueEntryRequest request =
                RandomModelGenerator.generate(UpdateQueueEntryRequest.class);

        request.setStatus(QueueStatus.FINISHED_SERVICE.toRef());
        request.setPriority(QueuePriority.URGENT.toRef());

        new CrudRequester(
                RequestSpecs.adminSpec(),
                Endpoint.QUEUE_ENTRY_UPDATE,
                ResponseSpecs.requestReturnsNotFound()
        ).update(NON_EXISTING_QUEUE_ENTRY_UUID, request);

        softly.assertThat(AdminSteps.getActiveQueueEntries().results())
                .noneMatch(entry -> NON_EXISTING_QUEUE_ENTRY_UUID.equals(entry.getUuid()));
    }
}
