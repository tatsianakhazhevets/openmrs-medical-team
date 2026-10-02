package apiParts.models.procedure;

import apiParts.models.HasUuid;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

// Metadata (not concepts), GET /proceduretype
@Getter
@RequiredArgsConstructor
public enum ProcedureType implements HasUuid {
    DENTAL("720b7b8d-3a02-47cb-9653-5901cb99f65b"),
    DIAGNOSTIC("61b05bd4-1b0a-440e-9e7b-fd3314015c1b"),
    EMERGENCY("7a063c89-1aa3-478f-b991-ad3cdc6edf6b"),
    IMAGING("b8bd2919-21c2-4f44-b5ea-8b40a124e33f"),
    LABORATORY("cfd229eb-f79d-407c-8e11-f62b6ab94825"),
    NURSING("41a2c62d-99d6-453f-966d-624736fc1088"),
    OBSTETRIC("16adc1d9-c907-472b-b6c6-e8a21f7ac025"),
    OTHER("ba8369fa-49db-4cc0-a730-9ab96eca75e1"),
    REFERRAL("11f3a801-f6e2-4fca-bd5b-96ca9a56ea32"),
    SURGICAL("d19ebec8-48c4-4c61-8156-a3925ef749c4"),
    THERAPEUTIC("2fd066f8-c6fa-4612-adbc-327bdff99fa4"),
    VACCINATION("16c2b343-6bcf-49d2-967f-b1872011002d");

    @JsonValue
    private final String uuid;
}
