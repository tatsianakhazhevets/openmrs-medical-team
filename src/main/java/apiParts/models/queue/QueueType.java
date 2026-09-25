package apiParts.models.queue;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum QueueType {
    OUTPATIENT_CONSULTATION("Outpatient Consultation");

    private final String display;
}
