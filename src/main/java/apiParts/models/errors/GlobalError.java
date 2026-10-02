package apiParts.models.errors;

/**
 * Validation error of OpenMRS REST API not bound to a field (validator uses errors.reject):
 * 400 + {"error": {"code": "webservices.rest.error.invalid.submission", "globalErrors": [{code, message}], "fieldErrors": {}}}
 * <p>
 * code = null: server does not return a code for this rule yet (known issue),
 * only 400 invalid submission can be checked.
 */
public interface GlobalError {
    String getCode();
}
