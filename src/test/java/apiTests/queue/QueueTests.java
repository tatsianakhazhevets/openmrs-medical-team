package apiTests.queue;

import apiParts.models.Location;
import apiParts.models.queue.QueuePriority;
import apiParts.models.queue.QueueStatus;
import apiParts.models.queue.QueueType;
import apiParts.models.encounter.Ref;
import apiParts.models.patient.CreatePatientResponse;
import apiParts.models.queue.GetQueueResponse;
import apiParts.models.queue.QueueResponse;
import apiParts.models.queueEntry.*;
import apiParts.models.visit.CreateVisitResponse;
import apiParts.skelethon.endpoints.Endpoint;
import apiParts.skelethon.requests.common.CrudRequester;
import apiParts.skelethon.requests.common.SuccessfulCrudRequester;
import apiParts.specs.RequestSpecs;
import apiParts.specs.ResponseSpecs;
import apiParts.steps.AdminSteps;
import apiTests.BaseTest;
import net.datafaker.Faker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;

public class QueueTests extends BaseTest {
    private static final String NON_EXISTING_QUEUE_ENTRY_UUID =
            "00000000-0000-0000-0000-000000000000";
    private static final Faker FAKER = new Faker(new Locale("en", "US"));
    private String patientUUID;
    private String visitUUID;
    private String queueEntryUUID;

    @BeforeEach
    public void setUp() {
        CreatePatientResponse patient = AdminSteps.createPatient();
        patientUUID = patient.getUuid();
        CreateVisitResponse visit = AdminSteps.createVisitWithRequiredFields(patientUUID);
        visitUUID = visit.getUuid();
    }

    @AfterEach
    void tearDown() {
        if (queueEntryUUID != null) {
            AdminSteps.endQueueEntry(queueEntryUUID);
        }
    }

    private GetQueueResponse getQueues() {
        return new SuccessfulCrudRequester<GetQueueResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.QUEUE_GET,
                ResponseSpecs.requestReturnsOk()
        ).get(Map.of(
                "v",
                "custom:(uuid,display,name,description,service:(uuid,display),allowedPriorities:(uuid,display),allowedStatuses:(uuid,display),location:(uuid,display))"
        ));
    }

    private QueueResponse getOutpatientConsultationQueue() {
        return getQueues().getResults().stream()
                .filter(queue ->
                        QueueType.OUTPATIENT_CONSULTATION.getDisplay().equals(queue.getName())
                                && Location.OUTPATIENT_CLINIC.getDisplay().equals(queue.getLocation().getDisplay())
                )
                .findFirst()
                .orElseThrow(() -> new AssertionError(
                        "Queue 'Outpatient Consultation' at 'Outpatient Clinic' was not found"
                ));
    }

    @Test
    void shouldAddPatientToQueue() {
        CreateQueueEntryResponse response = AdminSteps.addPatientToQueue(patientUUID, visitUUID);
        queueEntryUUID = response.getUuid();
        GetQueueEntryResponse queueEntries = AdminSteps.getActiveQueueEntries();
        QueueEntryResponse foundEntry = queueEntries.getResults().stream()
                .filter(entry -> entry.getUuid().equals(response.getUuid()))
                .findFirst()
                .orElseThrow(() -> new AssertionError(
                        "Created queue entry was not found in active queue"
                ));
        softly.assertThat(foundEntry.getUuid()).isEqualTo(response.getUuid());
        softly.assertThat(foundEntry.getPatient()).isEqualTo(response.getPatient());
        softly.assertThat(foundEntry.getVisit()).isEqualTo(response.getVisit());
        softly.assertThat(foundEntry.getStatus()).isEqualTo(response.getStatus());
        softly.assertThat(foundEntry.getPriority()).isEqualTo(response.getPriority());
        softly.assertThat(foundEntry.getStartedAt()).isEqualTo(response.getStartedAt());
        softly.assertThat(foundEntry.getEndedAt()).isEqualTo(response.getEndedAt());
        softly.assertAll();
    }

    @Test
    void shouldEndQueueEntry() {
        CreateQueueEntryResponse response = AdminSteps.addPatientToQueue(patientUUID, visitUUID);
        CreateQueueEntryResponse endedResponse = AdminSteps.endQueueEntry(response.getUuid());
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

        CreateQueueEntryResponse response = AdminSteps.addPatientToQueue(patientUUID, visitUUID);
        queueEntryUUID = response.getUuid();
        CreateQueueEntryResponse updatedResponse = AdminSteps.updateQueueEntry(
                response.getUuid(),
                status,
                priority,
                priorityComment
        );
        softly.assertThat(updatedResponse.getUuid()).isEqualTo(response.getUuid());
        softly.assertThat(updatedResponse.getStatus().getUuid()).isEqualTo(status.toRef().getUuid());
        softly.assertThat(updatedResponse.getStatus().getDisplay()).isEqualTo(status.toRef().getDisplay());
        softly.assertThat(updatedResponse.getPriority().getUuid()).isEqualTo(priority.toRef().getUuid());
        softly.assertThat(updatedResponse.getPriority().getDisplay()).isEqualTo(priority.toRef().getDisplay());
        softly.assertThat(updatedResponse.getPriorityComment()).isEqualTo(priorityComment);
        softly.assertAll();
    }

    @Test
    void shouldNotAddPatientToQueueWithoutPatient() {
        QueueResponse queue = getOutpatientConsultationQueue();
        CreateQueueEntryRequest request = CreateQueueEntryRequest.builder()
                .visit(Ref.of(visitUUID))
                .queueEntry(CreateQueueEntryRequest.QueueEntry.builder()
                        .status(QueueStatus.WAITING.toRef())
                        .priority(QueuePriority.NOT_URGENT.toRef())
                        .queue(Ref.of(queue.getUuid()))
                        .startedAt(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
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
}
