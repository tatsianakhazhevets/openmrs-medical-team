package apiParts.models.queue;

import apiParts.models.encounter.Ref;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum QueueStatus {
    WAITING(
            "51ae5e4d-b72b-4912-bf31-a17efb690aeb",
            "Waiting"
    ),
    FINISHED_SERVICE(
            "b559fb77-4e1e-4285-b9b7-1d03e0ba983f",
            "Finished Service");
    @JsonValue
    private final String uuid;
    private final String display;


    public Ref toRef() {
        return new Ref(uuid, display);
    }
}
