package apiTests.queue;

import apiParts.assertions.ModelAssertions;
import apiParts.models.Location;
import apiParts.models.encounter.Ref;
import apiParts.models.queue.*;
import apiParts.models.queueEntry.*;
import apiParts.skelethon.endpoints.Endpoint;
import apiParts.skelethon.requests.common.CrudRequester;
import apiParts.models.queueEntry.QueueEntrySearchParams;
import apiParts.skelethon.requests.search.SuccessfulSearchRequester;
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
        QueueEntryResponse response = AdminSteps.addPatientToQueue(patientUUID, visitUUID);
        queueEntryUUID = response.getUuid();
        GetQueueEntryResponse queueEntries = AdminSteps.getActiveQueueEntries();
        QueueEntryResponse foundEntry = queueEntries.getResults().stream()
                .filter(entry -> entry.getUuid().equals(response.getUuid()))
                .findFirst()
                .orElseThrow(() -> new AssertionError(
                        "Created queue entry was not found in active queue"
                ));
        ModelAssertions.assertMatchesExpected(softly, foundEntry, response, "queue entry");
        softly.assertAll();
    }

    @Test
    void shouldEndQueueEntry() {
        QueueEntryResponse response = AdminSteps.addPatientToQueue(patientUUID, visitUUID);
        QueueEntryResponse endedResponse = AdminSteps.endQueueEntry(response.getUuid());
        softly.assertThat(endedResponse.getUuid()).isEqualTo(response.getUuid());
        softly.assertThat(endedResponse.getEndedAt()).isNotNull();
        GetQueueEntryResponse activeEntries = AdminSteps.getActiveQueueEntries();
        softly.assertThat(activeEntries.getResults())
                .noneMatch(entry -> entry.getUuid().equals(response.getUuid()));
        softly.assertAll();
    }

    @Test
    void shouldUpdateQueueEntry() {
        String priorityComment = FAKER.text().text();
        QueuePriority priority = QueuePriority.URGENT;
        QueueStatus status = QueueStatus.FINISHED_SERVICE;

        QueueEntryResponse createdResponse = AdminSteps.addPatientToQueue(patientUUID, visitUUID);
        queueEntryUUID = createdResponse.getUuid();
        QueueEntryResponse updatedResponse = AdminSteps.updateQueueEntry(
                createdResponse.getUuid(),
                status,
                priority,
                priorityComment
        );
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

        CreateQueueEntryRequest request = CreateQueueEntryRequest.builder()
                .visit(Ref.of(visitUUID))
                .queueEntry(CreateQueueEntryRequest.QueueEntry.builder()
                        .status(QueueStatus.WAITING.toRef())
                        .priority(QueuePriority.NOT_URGENT.toRef())
                        .queue(Ref.of(queue.getUuid()))
                        .startedAt(DateTimeUtils.OPENMRS_RESPONSE_DATE_TIME
                                .withZone(ZoneOffset.UTC)
                                .format(Instant.now()))
                        .sortWeight(0)
                        .build())
                .build();

        new CrudRequester(
                RequestSpecs.adminSpec(),
                Endpoint.VISIT_QUEUE_ENTRY_POST,
                ResponseSpecs.requestReturnsInvalidSubmission("patient")
        ).create(request);
    }

    @Test
    void shouldNotUpdateNonExistingQueueEntry() {
        UpdateQueueEntryRequest request = UpdateQueueEntryRequest.builder()
                .status(QueueStatus.FINISHED_SERVICE.toRef())
                .priority(QueuePriority.URGENT.toRef())
                .priorityComment(FAKER.text().text())
                .build();
        new CrudRequester(
                RequestSpecs.adminSpec(),
                Endpoint.QUEUE_ENTRY_UPDATE,
                ResponseSpecs.requestReturnsNotFound()
        ).update(NON_EXISTING_QUEUE_ENTRY_UUID, request);
    }

    // ==== Search-based variant of AdminSteps.getActiveQueueEntries(). Original untouched. ====
    // The long custom:(...) projection moves out of an inline Map into a params object,
    // and the stream/filter/orElseThrow lookup becomes requireOne().
    @Test
    void addedQueueEntryIsFoundBySearchViaSearchRequester() {
        QueueEntryResponse response = AdminSteps.addPatientToQueue(patientUUID, visitUUID);
        queueEntryUUID = response.getUuid();

        QueueEntryResponse foundEntry = new SuccessfulSearchRequester<QueueEntryResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.QUEUE_ENTRY_GET,
                ResponseSpecs.requestReturnsOk())
                .search(QueueEntrySearchParams.builder()
                        .representation(QueueEntrySearchParams.ACTIVE_ENTRY_REPRESENTATION)
                        .location(Location.OUTPATIENT_CLINIC.getUuid())
                        .isEnded(false)
                        .build())
                .requireOne(entry -> entry.getUuid().equals(response.getUuid()),
                        "created queue entry " + response.getUuid());

        ModelAssertions.assertMatchesExpected(softly, foundEntry, response, "queue entry");
    }

}
