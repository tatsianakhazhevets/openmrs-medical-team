package apiParts.models;

/**
 * OpenMRS resource identified by uuid: metadata enums (implemented via Lombok @Getter)
 * and response models (implemented via Lombok @Data), see {@link apiParts.utils.Uuids}.
 */
public interface HasUuid {
    String getUuid();
}
