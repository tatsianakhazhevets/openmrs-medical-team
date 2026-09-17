package apiParts.models.errors;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

// Procedure errors without globalErrors / fieldErrors: 400 + {"error": {"message": "[<property> on class ... => <message>]"}}
@Getter
@RequiredArgsConstructor
public enum ProcedureErrorMessage {
    // startDateTime is not ISO-8601 (e.g. "" or "not-a-date")
    DATE_CONVERSION_ERROR("Error converting date"),

    // no auth: server fails on converting "patient" and returns 400 (not 401)
    PRIVILEGES_REQUIRED_GET_PATIENTS("Privileges required: Get Patients"),

    // GET /procedure without patient param
    OPERATION_NOT_SUPPORTED("The Resource Does not Support the Requested Operation"),

    // GET /procedure?patient={non-existent uuid} (KNOWN ISSUE: server returns 500 instead of 400)
    PATIENT_NOT_FOUND("Procedure.error.patientNotFound");

    private final String message;
}
