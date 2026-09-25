package apiParts.models.queue;

import apiParts.models.BaseModel;
import apiParts.models.encounter.Ref;
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
    public class QueueResponse extends BaseModel {

        private String uuid;
        private String display;
        private String name;
        private String description;

        private Ref service;
        private List<Ref> allowedPriorities;
        private List<Ref> allowedStatuses;
        private Ref location;
    }
