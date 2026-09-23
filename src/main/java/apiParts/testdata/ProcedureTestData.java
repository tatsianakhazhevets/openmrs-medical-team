package apiParts.testdata;

import apiParts.generators.RandomModelGenerator;
import apiParts.models.procedure.BodySite;
import apiParts.models.procedure.CreateProcedureRequest;
import apiParts.models.procedure.ProcedureConcept;
import apiParts.models.procedure.ProcedureStatus;
import apiParts.models.procedure.ProcedureType;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;

import static apiParts.utils.DateTimeUtils.MOSCOW;
import static apiParts.utils.DateTimeUtils.OPENMRS_REQUEST_DATE_TIME;

/**
 * Request of the standard procedure fixture (see apiParts.steps.AdminSteps#createProcedure).
 * <p>
 * PROCEDURE_START is fixed for the whole run, so procedureRequest() is equal to the request that
 * was actually sent and tests can build dates relative to the created procedure.
 */
public class ProcedureTestData {

    // How far in the past a procedure may start: any past date is valid for the server
    private static final int MIN_DAYS_AGO = 1;
    private static final int MAX_DAYS_AGO = 30;

    // Day in the past, truncated to minutes: server does not store milliseconds
    public static final OffsetDateTime PROCEDURE_START = OffsetDateTime.now(MOSCOW)
            .minusDays(RandomModelGenerator.randomInt(MIN_DAYS_AGO, MAX_DAYS_AGO))
            .truncatedTo(ChronoUnit.MINUTES);

    private ProcedureTestData() {
    }

    // Valid procedure with required fields only (Laparoscopic cholecystectomy, started in the past)
    public static CreateProcedureRequest procedureRequest(String patientUUID) {
        return CreateProcedureRequest.builder()
                .patient(patientUUID)
                .procedureCoded(ProcedureConcept.LAPAROSCOPIC_CHOLECYSTECTOMY.getUuid())
                .procedureType(ProcedureType.EMERGENCY.getUuid())
                .bodySite(BodySite.ABDOMEN.getUuid())
                .startDateTime(PROCEDURE_START.format(OPENMRS_REQUEST_DATE_TIME))
                .status(ProcedureStatus.COMPLETED.getUuid())
                .build();
    }
}
