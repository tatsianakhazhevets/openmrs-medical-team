package apiParts.models.order;

import apiParts.models.Link;
import apiParts.models.Ref;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderType {

    private String uuid;
    private String display;
    private String name;
    private String javaClassName;

    private Boolean retired;
    private String description;

    private List<Ref> conceptClasses;
    private Ref parent;

    private List<Link> links;

    private String resourceVersion;
}