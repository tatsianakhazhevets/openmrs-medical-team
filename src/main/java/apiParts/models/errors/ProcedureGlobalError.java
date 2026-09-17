package apiParts.models.errors;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

// emrapi ProcedureValidator errors (all are global, fieldErrors is empty).
// Non-existent uuid of patient / procedureType / concept is converted to null -> same "required" error
@Getter
@RequiredArgsConstructor
public enum ProcedureGlobalError implements GlobalError {
    // Required
    PATIENT_REQUIRED("Procedure.error.patientRequired"),
    PROCEDURE_TYPE_REQUIRED("Procedure.error.procedureTypeRequired"),
    PROCEDURE_REQUIRED("Procedure.error.procedureRequired"),
    BODY_SITE_REQUIRED("Procedure.error.bodySiteRequired"),
    STATUS_REQUIRED("Procedure.error.statusRequired"),
    START_DATE_TIME_REQUIRED("Procedure.error.startDateTimeRequired"),

    // Combinations of fields
    PROCEDURE_CODED_AND_NON_CODED_MUTUALLY_EXCLUSIVE("Procedure.error.procedureCodedAndNonCodedMutuallyExclusive"),
    START_DATE_TIME_AND_ESTIMATED_DATE_MUTUALLY_EXCLUSIVE("Procedure.error.startDateTimeAndEstimatedDateMutuallyExclusiveForNewProcedures"),
    END_DATE_TIME_BEFORE_START_DATE_TIME("Procedure.error.endDateTimeBeforeStartDateTime"),
    DURATION_UNIT_REQUIRED("Procedure.error.durationUnitRequired"),

    // KNOWN ISSUE: server accepts start date in the future (201), expected error code is unknown
    START_DATE_TIME_IN_FUTURE(null);

    private final String code;
}
