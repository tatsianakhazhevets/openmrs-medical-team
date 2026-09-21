package apiParts.generators;

import java.util.UUID;

public class RandomUuidGenerator {
    public static String generateUuid() {
        return UUID.randomUUID().toString();
    }
}