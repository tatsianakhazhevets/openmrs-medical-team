package apiParts.models.encounter;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EncounterType {
    ENCOUNTER_TYPE_UUID("81852aee-3f10-11e8-b467-0ed5f89f718b");

    private final String uuid;
}