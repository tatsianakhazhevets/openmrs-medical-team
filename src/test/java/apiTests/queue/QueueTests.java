package apiTests.queue;

import apiParts.assertions.ModelAssertions;
import apiParts.generators.RandomModelGenerator;
import apiParts.models.Ref;
import apiParts.models.queue.*;
import apiParts.models.queueEntry.*;
import apiParts.skelethon.endpoints.Endpoint;
import apiParts.models.search.SearchResult;
import apiParts.skelethon.requests.crud.CrudRequester;
import apiParts.specs.RequestSpecs;
import apiParts.specs.ResponseSpecs;
import apiParts.steps.QueueSteps;
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
            QueueSteps.endQueueEntry(queueEntryUUID);
        }
    }

    @Test
    void shouldAddPatientToQueue() {
        QueueEntryResponse response =
                QueueSteps.addPatientToQueue(patientUUID, visitUUID);

        queueEntryUUID = response.getUuid();

        QueueEntryResponse foundEntry = QueueSteps.getActiveQueueEntries().requireOne(
                entry -> entry.getUuid().equals(response.getUuid()),
                "created queue entry " + response.getUuid());
        ModelAssertions.assertMatchesExpectedIgnoringFields(
                foundEntry,
                response,
                "queue entry",
                "queue.links",
                "status.links",
                "patient.links",
                "visit.links",
                "priority.links");
    }

    @Test
    void shouldEndQueueEntry() {
        QueueEntryResponse response =
                QueueSteps.addPatientToQueue(patientUUID, visitUUID);

        EndQueueEntryRequest endRequest =
                RandomModelGenerator.generate(EndQueueEntryRequest.class);
        QueueEntryResponse endedResponse =
                QueueSteps.endQueueEntry(response.getUuid(), endRequest);

        softly.assertThat(endedResponse.getUuid()).isEqualTo(response.getUuid());
        softly.assertThat(endedResponse.getEndedAt()).isNotNull();
        SearchResult<QueueEntryResponse> activeEntries = QueueSteps.getActiveQueueEntries();
        softly.assertThat(activeEntries.results()).noneMatch(entry -> entry.getUuid().equals(response.getUuid()));
    }

    @Test
    void shouldUpdateQueueEntry() {
        QueuePriority priority = QueuePriority.URGENT;
        QueueStatus status = QueueStatus.FINISHED_SERVICE;

        QueueEntryResponse createdResponse =
                QueueSteps.addPatientToQueue(patientUUID, visitUUID);
        queueEntryUUID = createdResponse.getUuid();

        UpdateQueueEntryRequest updateRequest =
                RandomModelGenerator.generate(UpdateQueueEntryRequest.class);
        updateRequest.setStatus(status.toRef());
        updateRequest.setPriority(priority.toRef());

        QueueSteps.updateQueueEntry(createdResponse.getUuid(), updateRequest);

        QueueEntryResponse expected = new QueueEntryResponse();
        expected.setStatus(status.toRef());
        expected.setPriority(priority.toRef());

        QueueEntryResponse foundEntry = QueueSteps.getActiveQueueEntries().requireOne(
                entry -> entry.getUuid().equals(createdResponse.getUuid()),
                "updated queue entry " + createdResponse.getUuid());

        ModelAssertions.assertMatchesExpected(foundEntry, expected, "updated queue entry");
    }

    @Test
    void shouldNotAddPatientToQueueWithoutPatient() {
        QueueResponse queue = QueueSteps.getOutpatientConsultationQueue();

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
                ResponseSpecs.requestReturnsInvalidSubmissionPatient()).create(request);

        softly.assertThat(QueueSteps.getActiveQueueEntries().results())
                .noneMatch(entry -> entry.getVisit() != null && visitUUID.equals(entry.getVisit().getUuid()));
    }

    @Test
    void shouldNotUpdateNonExistingQueueEntry() {
        String nonExistingQueueEntryUuid =
                RandomModelGenerator.randomUnknownUuid();
        UpdateQueueEntryRequest request =
                RandomModelGenerator.generate(UpdateQueueEntryRequest.class);

        request.setStatus(QueueStatus.FINISHED_SERVICE.toRef());
        request.setPriority(QueuePriority.URGENT.toRef());

        new CrudRequester(
                RequestSpecs.adminSpec(),
                Endpoint.QUEUE_ENTRY_UPDATE,
                ResponseSpecs.requestReturnsNotFound()
        ).update(nonExistingQueueEntryUuid, request);

        softly.assertThat(QueueSteps.getActiveQueueEntries().results())
                .noneMatch(entry -> nonExistingQueueEntryUuid.equals(entry.getUuid()));
    }
}
