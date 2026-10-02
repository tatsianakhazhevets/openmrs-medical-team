package apiParts.steps;

import apiParts.generators.RandomModelGenerator;
import apiParts.models.Location;
import apiParts.models.Ref;
import apiParts.models.queue.*;
import apiParts.models.queueEntry.*;
import apiParts.models.search.SearchResult;
import apiParts.skelethon.endpoints.Endpoint;
import apiParts.skelethon.requests.crud.SuccessfulCrudRequester;
import apiParts.skelethon.requests.search.SuccessfulSearchRequester;
import apiParts.specs.RequestSpecs;
import apiParts.specs.ResponseSpecs;

public class QueueSteps {
    public static QueueEntryResponse addPatientToQueue(
            String patientUUID,
            String visitUUID) {

        QueueResponse queue = getOutpatientConsultationQueue();

        CreateQueueEntryRequest request =
                RandomModelGenerator.generate(CreateQueueEntryRequest.class);

        request.setVisit(Ref.of(visitUUID));

        CreateQueueEntryRequest.QueueEntry queueEntry =
                RandomModelGenerator.generate(CreateQueueEntryRequest.QueueEntry.class);

        queueEntry.setStatus(QueueStatus.WAITING.toRef());
        queueEntry.setPriority(QueuePriority.NOT_URGENT.toRef());
        queueEntry.setQueue(Ref.of(queue.getUuid()));
        queueEntry.setPatient(Ref.of(patientUUID));

        request.setQueueEntry(queueEntry);

        return new SuccessfulCrudRequester<QueueEntryResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.VISIT_QUEUE_ENTRY_POST,
                ResponseSpecs.requestReturnsCreated()
        ).create(request);
    }

    public static QueueEntryResponse updateQueueEntry(
            String queueEntryUUID,
            UpdateQueueEntryRequest request) {

        return new SuccessfulCrudRequester<QueueEntryResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.QUEUE_ENTRY_UPDATE,
                ResponseSpecs.requestReturnsOk()
        ).update(queueEntryUUID, request);
    }

    public static QueueEntryResponse endQueueEntry(
            String queueEntryUUID,
            EndQueueEntryRequest request) {

        return new SuccessfulCrudRequester<QueueEntryResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.QUEUE_ENTRY_UPDATE,
                ResponseSpecs.requestReturnsOk()
        ).update(queueEntryUUID, request);
    }

    public static QueueEntryResponse endQueueEntry(String queueEntryUUID) {
        EndQueueEntryRequest request =
                RandomModelGenerator.generate(EndQueueEntryRequest.class);

        return endQueueEntry(queueEntryUUID, request);
    }

    public static SearchResult<QueueEntryResponse> getActiveQueueEntries() {
        return new SuccessfulSearchRequester<QueueEntryResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.QUEUE_ENTRY_GET,
                ResponseSpecs.requestReturnsOk()
        ).search(QueueEntrySearchParams.builder()
                .representation(QueueEntrySearchParams.ACTIVE_ENTRY_REPRESENTATION)
                .location(Location.OUTPATIENT_CLINIC.getUuid())
                .isEnded(false)
                .build());
    }

    public static QueueResponse getOutpatientConsultationQueue() {
        return new SuccessfulSearchRequester<QueueResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.QUEUE_GET,
                ResponseSpecs.requestReturnsOk()
        ).search(QueueSearchParams.builder()
                        .representation(QueueSearchParams.QUEUE_LOOKUP_REPRESENTATION)
                        .build())
                .requireOne(queue ->
                                QueueType.OUTPATIENT_CONSULTATION.getDisplay().equals(queue.getName())
                                        && Location.OUTPATIENT_CLINIC.getDisplay().equals(queue.getLocation().getDisplay()),
                        "Queue 'Outpatient Consultation' at 'Outpatient Clinic'");
    }
}
