package apiParts.models.euncouterTest;

import apiParts.generators.EnumGeneratingRule;
import apiParts.generators.StringGeneratingRule;
import apiParts.models.Location;

public class EncounterTestRequest {

    private String patient;

    @EnumGeneratingRule(enumClass = EncounterTypeForApi.class, valueMethod = "getUuid")
    private String encounterType;

    @StringGeneratingRule(regex = "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}[+-]\\d{4}$")
    private String encounterDatetime;

    @EnumGeneratingRule(enumClass = Location.class, valueMethod = "getUuid")
    private String location;
}