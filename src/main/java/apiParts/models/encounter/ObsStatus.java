package apiParts.models.encounter;

// org.openmrs.Obs.Status, used for obs recorded against a test order (e.g. lab results).
// Default enum JSON serialization (name()) matches the values expected/returned by the server.
public enum ObsStatus {
    PRELIMINARY,
    FINAL,
    AMENDED
}
