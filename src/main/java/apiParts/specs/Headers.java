package apiParts.specs;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Headers {

    AUTHORIZATION("Authorization");

    private final String header;
}