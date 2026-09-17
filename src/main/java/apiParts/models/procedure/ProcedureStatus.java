package apiParts.models.procedure;

import apiParts.models.HasUuid;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

// Concepts for procedure.status: answers to "Procedure status" (f0d47b45-8303-4cdc-a9f2-c37135a3700f)
// GET /concept?answerTo=f0d47b45-8303-4cdc-a9f2-c37135a3700f. Not listed yet: On hold, Discontinued, Unknown, Entered in error
@Getter
@RequiredArgsConstructor
public enum ProcedureStatus implements HasUuid {
    COMPLETED("1267AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"),
    IN_PROGRESS("163723AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"),
    NOT_DONE("1118AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"),
    PREPARATION("167153AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");

    @JsonValue
    private final String uuid;
}
