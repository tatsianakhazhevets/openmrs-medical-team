package apiParts.models.euncouterTest;

import apiParts.models.BaseModel;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class GetEncounterResponse extends BaseModel {
    private String uuid;
    private String display;
    private String encounterDatetime;
    private EncounterPatientResponse patient;
    private EncounterLocationGetResponse location;
    private Object form;
    private GetEncounterTypeResponse encounterType;
    private List<Object> obs;
    private List<Object> orders;
    private boolean voided;
    private AuditInfoResponse auditInfo;
    private Object visit;
    private List<Object> encounterProviders;
    private List<Object> diagnoses;
    private List<EncounterLinkResponse> links;
    private String resourceVersion;
}