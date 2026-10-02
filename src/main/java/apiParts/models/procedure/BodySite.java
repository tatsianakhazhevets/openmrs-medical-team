package apiParts.models.procedure;

import apiParts.models.HasUuid;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

// Concepts for procedure.bodySite. UI searches concept class "Anatomy", server accepts any concept
@Getter
@RequiredArgsConstructor
public enum BodySite implements HasUuid {
    ABDOMEN("1808AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"),
    CHEST("1349AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"),
    EYE("164386AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"),
    SKIN("ec7f69e8-493b-4795-8b18-cba630d49b27");     // not CIEL uuid (local dictionary)

    @JsonValue
    private final String uuid;
}
