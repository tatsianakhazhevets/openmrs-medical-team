package apiParts.models.auth;

import apiParts.models.BaseModel;
import apiParts.models.Ref;
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
public class LoginAdminResponse extends BaseModel {
    private String sessionId;
    private boolean authenticated;
    private User user;
    private String person;
    private List<String> allowedLocales;
    private SessionLocation sessionLocation;
    private Ref currentProvider;
}