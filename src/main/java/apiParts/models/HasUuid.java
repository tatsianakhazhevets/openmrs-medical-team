package apiParts.models;

/**
 * Enum of OpenMRS metadata identified by uuid (implemented via Lombok @Getter).
 * Lets helpers map any such enum to a {@link apiParts.models.encounter.Ref}.
 */
public interface HasUuid {
    String getUuid();
}
