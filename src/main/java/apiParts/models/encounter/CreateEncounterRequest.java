package apiParts.models.encounter;

import apiParts.generators.EnumGeneratingRule;
import apiParts.generators.GeneratedBy;
import apiParts.generators.SkipGeneration;
import apiParts.generators.suppliers.CurrentPatientUuid;
import apiParts.generators.suppliers.RandomVitalsObs;
import apiParts.models.BaseModel;
import apiParts.models.EncounterType;
import apiParts.models.HasUuid;
import apiParts.models.Location;
import apiParts.models.VitalsConcept;
import apiParts.models.order.LabTestConcept;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.List;

import static apiParts.generators.GenerationProfile.ORDER;
import static apiParts.generators.GenerationProfile.VITALS;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
// RandomModelGenerator: generate with a profile, e.g. generate(CreateEncounterRequest.class, VITALS)
public class CreateEncounterRequest extends BaseModel {
    @GeneratedBy(CurrentPatientUuid.class)
    private String patient;  // patient uuid, created in test setup

    @EnumGeneratingRule(enumClass = EncounterType.class, only = "VITALS", profiles = VITALS)
    @EnumGeneratingRule(enumClass = EncounterType.class, only = "ORDER", profiles = ORDER)
    private EncounterType encounterType;

    @SkipGeneration
    private String visit;              // visit uuid, optional

    @SkipGeneration
    private String encounterDatetime;  // ISO-8601, must have if visit is present

    private Location location;         // any location

    @GeneratedBy(value = RandomVitalsObs.class, profiles = VITALS)
    @SkipGeneration(profiles = ORDER)
    private List<Obs> obs;

    // ORDER: orders are passed in overrides (OrderTestData.orderEncounterRequest), they depend on patient and orderer
    @SkipGeneration
    private List<Object> orders;       // DrugOrder, TestOrder, etc.

    @Data
    @NoArgsConstructor
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Obs {
        private HasUuid concept;
        private Object value;
        private ObsStatus status;  // set only for lab result obs, null (and omitted) for vitals
        private String order;      // testorder uuid, set only for lab result obs

        public static Obs of(VitalsConcept concept, Number value) {
            requireType(concept, VitalsConcept.ValueType.NUMERIC);
            return new Obs(concept, value, null, null);
        }

        public static Obs of(VitalsConcept concept, String value) {
            requireType(concept, VitalsConcept.ValueType.TEXT);
            return new Obs(concept, value, null, null);
        }

        // lab test result: value recorded against a testorder, status is always FINAL
        public static Obs ofLabResult(LabTestConcept concept, String orderUuid, Number value) {
            return new Obs(concept, value, ObsStatus.FINAL, orderUuid);
        }

        private static void requireType(VitalsConcept c, VitalsConcept.ValueType expected) {
            if (c.getValueType() != expected) {
                throw new IllegalArgumentException(c + " expects " + c.getValueType());
            }
        }
    }
}
