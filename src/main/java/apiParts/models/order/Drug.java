package apiParts.models.order;

import apiParts.models.HasUuid;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

// GET /drug/{uuid}: drug is bound to a concept, order.concept must match drug.concept
@Getter
@RequiredArgsConstructor
public enum Drug implements HasUuid {
    ASPIRIN_325MG("38087db3-7395-431f-88d5-bb25e06e33f1", "71617AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"),
    ACETAMINOPHEN_325MG("dfd36a48-1946-454c-bc04-8dc7cada7120", "70116AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"),
    ACYCLOVIR_CREAM_3("a74fe93c-b023-4159-8bd6-5d40a0909636", "70245AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");   // not a tablet

    @JsonValue
    private final String uuid;
    private final String conceptUuid;
}
