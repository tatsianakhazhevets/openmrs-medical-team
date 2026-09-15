package apiParts.models.patient;

import apiParts.models.BaseModel;
import apiParts.models.EncounterType;
import apiParts.models.Location;
import apiParts.models.VitalsConcept;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreateEncounterRequest extends BaseModel {
    private String patient;  // patient uuid, created in test setup
    private EncounterType encounterType;
    private Location location;
    private List<Obs> obs;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Obs {
        private VitalsConcept concept;
        private Object value;

        public static Obs of(VitalsConcept concept, Number value) {
            requireType(concept, VitalsConcept.ValueType.NUMERIC);
            return new Obs(concept, value);
        }

        public static Obs of(VitalsConcept concept, String value) {
            requireType(concept, VitalsConcept.ValueType.TEXT);
            return new Obs(concept, value);
        }

        private static void requireType(VitalsConcept c, VitalsConcept.ValueType expected) {
            if (c.getValueType() != expected) {
                throw new IllegalArgumentException(c + " expects " + c.getValueType());
            }
        }
    }
}
