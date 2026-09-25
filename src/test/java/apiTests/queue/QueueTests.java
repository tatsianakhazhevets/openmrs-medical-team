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
import apiParts.utils.DateTimeUtils;
import apiTests.BaseTest;
import common.annotations.CreatePatient;
import common.annotations.CreateVisit;
import common.storages.SessionStorage;
import net.datafaker.Faker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Locale;

@CreateVisit
@CreatePatient
public class QueueTests extends BaseTest {
    private static final String NON_EXISTING_QUEUE_ENTRY_UUID =
            "00000000-0000-0000-0000-000000000000";
    private static final Faker FAKER = new Faker(new Locale("en", "US"));
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
        softly.assertAll();
    }

    @Test
    void shouldEndQueueEntry() {
        QueueEntryResponse response =
                AdminSteps.addPatientToQueue(patientUUID, visitUUID);

        EndQueueEntryRequest endRequest =
                RandomModelGenerator.generate(EndQueueEntryRequest.class);

        endRequest.setEndedAt(
                DateTimeUtils.UTC_DATE_TIME
                        .withZone(ZoneOffset.UTC)
                        .format(Instant.now())
        );
        QueueEntryResponse endedResponse =
                AdminSteps.endQueueEntry(response.getUuid(), endRequest);

        softly.assertThat(endedResponse.getUuid()).isEqualTo(response.getUuid());
        softly.assertThat(endedResponse.getEndedAt()).isNotNull();
        SearchResult<QueueEntryResponse> activeEntries = AdminSteps.getActiveQueueEntries();
        softly.assertThat(activeEntries.results()).noneMatch(entry -> entry.getUuid().equals(response.getUuid()));
        softly.assertAll();
    }

    @Test
    void shouldUpdateQueueEntry() {
        String priorityComment = FAKER.text().text();
        QueuePriority priority = QueuePriority.URGENT;
        QueueStatus status = QueueStatus.FINISHED_SERVICE;

        QueueEntryResponse createdResponse =
                AdminSteps.addPatientToQueue(patientUUID, visitUUID);
        queueEntryUUID = createdResponse.getUuid();

        UpdateQueueEntryRequest updateRequest = RandomModelGenerator.generate(UpdateQueueEntryRequest.class);
        updateRequest.setStatus(status.toRef());
        updateRequest.setPriority(priority.toRef());
        updateRequest.setPriorityComment(priorityComment);

        QueueEntryResponse updatedResponse =
                AdminSteps.updateQueueEntry(createdResponse.getUuid(), updateRequest);

        QueueEntryResponse expected = new QueueEntryResponse();
        expected.setStatus(status.toRef());
        expected.setPriority(priority.toRef());
        expected.setPriorityComment(priorityComment);
        softly.assertThat(updatedResponse.getUuid()).isEqualTo(createdResponse.getUuid());
        ModelAssertions.assertMatchesExpected(softly, updatedResponse, expected, "updated queue entry");
        softly.assertAll();
    }

    @Test
    void shouldNotAddPatientToQueueWithoutPatient() {
        QueueResponse queue = AdminSteps.getOutpatientConsultationQueue();

        CreateQueueEntryRequest request =
                RandomModelGenerator.generate(CreateQueueEntryRequest.class);
        request.setVisit(Ref.of(visitUUID));

        CreateQueueEntryRequest.QueueEntry queueEntry = RandomModelGenerator.generate(CreateQueueEntryRequest.QueueEntry.class);
        queueEntry.setStatus(QueueStatus.WAITING.toRef());
        queueEntry.setPriority(QueuePriority.NOT_URGENT.toRef());
        queueEntry.setQueue(Ref.of(queue.getUuid()));
        queueEntry.setPatient(null);
        queueEntry.setStartedAt(DateTimeUtils.OPENMRS_RESPONSE_DATE_TIME
                .withZone(ZoneOffset.UTC).format(Instant.now()));
        queueEntry.setSortWeight(0);

        request.setQueueEntry(queueEntry);

        new CrudRequester(
                RequestSpecs.adminSpec(),
                Endpoint.VISIT_QUEUE_ENTRY_POST,
                ResponseSpecs.requestReturnsInvalidSubmission("patient")
        ).create(request);
    }

    @Test
    void shouldNotUpdateNonExistingQueueEntry() {
        UpdateQueueEntryRequest request =
                RandomModelGenerator.generate(UpdateQueueEntryRequest.class);

        request.setStatus(QueueStatus.FINISHED_SERVICE.toRef());
        request.setPriority(QueuePriority.URGENT.toRef());
        request.setPriorityComment(FAKER.text().text());

        new CrudRequester(
                RequestSpecs.adminSpec(),
                Endpoint.QUEUE_ENTRY_UPDATE,
                ResponseSpecs.requestReturnsNotFound()
        ).update(NON_EXISTING_QUEUE_ENTRY_UUID, request);
    }
}
