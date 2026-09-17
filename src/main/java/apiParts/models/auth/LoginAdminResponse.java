package apiParts.models.auth;

import apiParts.models.BaseModel;
import apiParts.models.User;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class LoginAdminResponse extends BaseModel {
    private String sessionId;
    private boolean authenticated;
    private User user;
}