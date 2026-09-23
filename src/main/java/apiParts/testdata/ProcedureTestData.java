package apiParts.testdata;

import apiParts.generators.RandomModelGenerator;
import apiParts.models.procedure.CreateProcedureRequest;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;

import static apiParts.utils.DateTimeUtils.MOSCOW;

/**
 * Request of the standard procedure fixture (see apiParts.steps.AdminSteps#createProcedure).
 * <p>
 * Request is random (see CreateProcedureRequest generating rules): the one actually sent by @CreateProcedure
 * is kept in SessionStorage.getProcedureRequest(). PROCEDURE_START is fixed for the whole run,
 * so tests can build dates relative to the created procedure.
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

    // Valid random coded procedure started in the past (PROCEDURE_START), without end date and duration
    public static CreateProcedureRequest procedureRequest(String patientUUID) {
        return RandomModelGenerator.generate(CreateProcedureRequest.class, Map.of("patient", patientUUID));
    }
}
