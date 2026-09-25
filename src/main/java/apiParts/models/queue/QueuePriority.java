package apiParts.models.queue;

import apiParts.models.encounter.Ref;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum QueuePriority {
    NOT_URGENT(
            "f4620bfa-3625-4883-bd3f-84c2cce14470",
            "Not Urgent"
    ),
    URGENT(
            "dc3492ef-24a5-4fd9-b58d-4fd2acf7071f",
            "Urgent"
    );
    @JsonValue
    private final String uuid;
    private final String display;

    public Ref toRef() {
        return new Ref(uuid, display);
    }
}
