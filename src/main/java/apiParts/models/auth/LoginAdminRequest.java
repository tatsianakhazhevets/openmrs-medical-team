package apiParts.models.auth;

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
    private String username;
    private String password;
}