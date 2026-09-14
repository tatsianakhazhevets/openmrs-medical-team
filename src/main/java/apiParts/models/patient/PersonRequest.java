package apiParts.models.patient;

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
public class PersonRequest {
    private String gender;
    private Integer age;
    private String birthdate;
    private Boolean birthdateEstimated;
    private Boolean dead;
    private String deathDate;
    private String causeOfDeath;
    private List<PersonName> names;
    private List<PersonAddress> addresses;
}