package apiParts.models.errors;

/**
 * Validation error of OpenMRS REST API:
 * 400 + {"error": {"code": "webservices.rest.error.invalid.submission", "fieldErrors": {field: [{code}]}}}
 * <p>
 * code = null: server does not return a code for this rule yet (known issue),
 * only presence of the field in fieldErrors can be checked.
 */
public interface FieldError {
    String getField();

    String getCode();
}
