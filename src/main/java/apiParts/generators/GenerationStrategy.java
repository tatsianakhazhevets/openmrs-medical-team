package apiParts.generators;

public enum GenerationStrategy {
    RANDOM,
    IDENTIFIER,
    PATIENT_UUID,
    PROVIDER_UUID,
    VITALS_OBSERVATIONS,
    DATE,
    DATE_TIME,
    DATE_RANGE_START,
    DATE_RANGE_END,
    DURATION,
    DEPENDS_ON_FIELD
}
