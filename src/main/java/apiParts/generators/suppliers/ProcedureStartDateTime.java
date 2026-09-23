package apiParts.generators.suppliers;

import apiParts.testdata.ProcedureTestData;

import java.util.function.Supplier;

import static apiParts.utils.DateTimeUtils.OPENMRS_REQUEST_DATE_TIME;

// Start of procedure in the past, fixed for the whole run (see ProcedureTestData.PROCEDURE_START)
public class ProcedureStartDateTime implements Supplier<String> {
    @Override
    public String get() {
        return ProcedureTestData.PROCEDURE_START.format(OPENMRS_REQUEST_DATE_TIME);
    }
}
