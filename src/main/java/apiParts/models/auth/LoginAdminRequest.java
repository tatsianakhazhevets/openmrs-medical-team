package apiParts.models.auth;

import apiParts.generators.GeneratingRule;
import apiParts.models.BaseModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoginAdminRequest extends BaseModel {
    @GeneratingRule(property = "adminUsername")
    private String username;

    @GeneratingRule(property = "adminPassword")
    private String password;
}