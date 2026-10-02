package apiParts.models.procedure;

import apiParts.models.HasUuid;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

// Concepts for procedureCoded. UI searches concept class "Procedure", server accepts any concept
@Getter
@RequiredArgsConstructor
public enum ProcedureConcept implements HasUuid {
    LAPAROSCOPIC_CHOLECYSTECTOMY("162897AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"),     // class Procedure
    X_RAY_CHEST("12AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"),                      // class Radiology/Imaging Procedure
    INFLUENZA_VACCINATION("2967590d-4fdb-4838-905d-edfc19a1c587");          // class Procedure, not CIEL uuid (local dictionary)

    @JsonValue
    private final String uuid;
}
