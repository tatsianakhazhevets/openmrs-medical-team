package apiParts.models.auth;

import apiParts.models.Ref;
import apiParts.models.auth.Role;
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
public class User {
    private String uuid;
    private String display;
    private String username;
    private String systemId;
    private UserProperties userProperties;
    private Ref person;
    private List<Role> roles;
    private List<String> privileges;
}