package apiParts.models.vitals;

import apiParts.generators.RandomModelGenerator;
import apiParts.models.HasUuid;
import apiParts.models.VitalsConcept;
import apiParts.models.encounter.ObsStatus;
import apiParts.models.order.LabTestConcept;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Obs {
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

    // value inside the absolute range of the concept (nominal range when it has no limits), text for TEXT concepts
    public static Obs random(VitalsConcept concept) {
        if (concept.getValueType() == VitalsConcept.ValueType.TEXT) {
            return of(concept, RandomModelGenerator.randomSentence());
        }
        return of(concept, concept.valueOf(RandomModelGenerator.randomDouble(
                concept.low(), concept.high(), concept.getDecimalPlaces())));
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