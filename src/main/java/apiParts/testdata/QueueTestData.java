package apiParts.testdata;

import apiParts.models.encounter.Ref;
import apiParts.models.queue.QueuePriority;
import apiParts.models.queue.QueueStatus;
import apiParts.models.queueEntry.CreateQueueEntryRequest;
import apiParts.models.queueEntry.EndQueueEntryRequest;
import apiParts.models.queueEntry.UpdateQueueEntryRequest;
import apiParts.utils.DateTimeUtils;

import java.time.Instant;
import java.time.ZoneOffset;

/**
 * Requests of the standard queue entry fixtures (see apiParts.steps.AdminSteps).
 */
public class QueueTestData {

    // First entry of a freshly created queue has no competing priority - 0 is a valid default
    private static final int INITIAL_SORT_WEIGHT = 0;

    private QueueTestData() {
    }

    // Puts a patient into the given queue: Waiting status, Not Urgent priority, started now
    public static CreateQueueEntryRequest queueEntryRequest(String queueUUID, String visitUUID, String patientUUID) {
        return CreateQueueEntryRequest.builder()
                .visit(Ref.of(visitUUID))
                .queueEntry(CreateQueueEntryRequest.QueueEntry.builder()
                        .status(QueueStatus.WAITING.toRef())
                        .priority(QueuePriority.NOT_URGENT.toRef())
                        .queue(Ref.of(queueUUID))
                        .patient(Ref.of(patientUUID))
                        .startedAt(DateTimeUtils.OPENMRS_RESPONSE_DATE_TIME.withZone(ZoneOffset.UTC).format(Instant.now()))
                        .sortWeight(INITIAL_SORT_WEIGHT)
                        .build())
                .build();
    }

    public static UpdateQueueEntryRequest updateQueueEntryRequest(
            QueueStatus status, QueuePriority priority, String priorityComment) {
        return UpdateQueueEntryRequest.builder()
                .status(status.toRef())
                .priority(priority.toRef())
                .priorityComment(priorityComment)
                .build();
    }

    // Ends a queue entry as of now
    public static EndQueueEntryRequest endQueueEntryRequest() {
        return EndQueueEntryRequest.builder()
                .endedAt(DateTimeUtils.UTC_DATE_TIME.withZone(ZoneOffset.UTC).format(Instant.now()))
                .build();
    }
}
