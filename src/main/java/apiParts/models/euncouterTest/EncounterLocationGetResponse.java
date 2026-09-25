package apiParts.models.euncouterTest;

import apiParts.models.BaseModel;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class EncounterLocationGetResponse extends BaseModel  {
    private String uuid;
    private String display;
    private String name;
    private String description;
    private String address1;
    private String address2;
    private String cityVillage;
    private String stateProvince;
    private String country;
    private String postalCode;
    private String latitude;
    private String longitude;
    private String countyDistrict;
    private List<LocationTagResponse> tags;
    private Object parentLocation;
    private List<Object> childLocations;
    private boolean retired;
    private List<Object> attributes;
    private String address3;
    private String address4;
    private String address5;
    private String address6;
    private String address7;
    private String address8;
    private String address9;
    private String address10;
    private String address11;
    private String address12;
    private String address13;
    private String address14;
    private String address15;
    private List<EncounterLinkResponse> links;
    private String resourceVersion;
}