package apiParts.models.euncouterTest;

import apiParts.models.BaseModel;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class EncounterTestResponse extends BaseModel {
    private String uuid;
    private String display;
    private String encounterDatetime;

    private EncounterPatientResponse patient;
    private EncounterLocationResponse location;

    private Object form;

    private EncounterTypeResponse encounterType;

    private List<Object> obs;
    private List<Object> orders;

    private boolean voided;

    private Object visit;

    private List<Object> encounterProviders;
    private List<Object> diagnoses;

    private List<EncounterLinkResponse> links;

    private String resourceVersion;
}