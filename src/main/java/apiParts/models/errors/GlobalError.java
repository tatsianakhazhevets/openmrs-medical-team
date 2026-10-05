package apiParts.models.errors;

/**
 * Validation error of OpenMRS REST API not bound to a field (validator uses errors.reject):
 * 400 + {"error": {"code": "webservices.rest.error.invalid.submission", "globalErrors": [{code, message}], "fieldErrors": {}}}
 */
public interface GlobalError {
    String getCode();
}
