package apiParts.models.errors;

/**
 * Validation error of OpenMRS REST API:
 * 400 + {"error": {"code": "webservices.rest.error.invalid.submission", "fieldErrors": {field: [{code}]}}}
 */
public interface FieldError {
    String getField();

    String getCode();
}
