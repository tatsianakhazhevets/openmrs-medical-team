package apiParts.models.euncouterTest;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EncounterTypeForApi {

    ADMISSION("e22e39fd-7db2-45e7-80f1-60fa0d5a4378"),
    ADULT_VISIT("0e8230ce-bd1d-43f5-a863-cf44344fa4b0"),
    ATTACHMENT_UPLOAD("5021b1a1-e7f6-44b4-ba02-da2f2bcf8718"),
    BED_ASSIGNMENT("f47ac10b-58cc-4372-a567-0e02b2c3d479"),
    CANCEL_ADT("550e8400-e29b-41d4-a716-446655440000"),
    CHECK_IN("ca3aed11-1aa4-42a1-b85c-8332fc8001fc"),
    CHECK_OUT("25a042b2-60bc-4940-a909-debd098b7d82"),
    CONSULTATION("dd528487-82a5-4082-9c72-ed246bd49591"),
    DISCHARGE("181820aa-88c9-479b-9077-af92f5364329"),
    IMMUNIZATIONS("29c02aff-9a93-46c9-bf6f-48b552fcb1fa"),
    INPATIENT_NOTE("a1f5c3d2-4b6e-4e8a-9f2d-1b3e8e4a2d7f"),
    INTRA_HOSPITAL_TRANSFER("7b68d557-85ef-4fc8-b767-4fa4f5eb5c23"),
    LAB_RESULTS("3596fafb-6f6f-4396-8c87-6e63a0f1bd71"),
    MENTAL_HEALTH_ASSESSMENT("36db5123-0ad5-41c0-9037-625b46e0ceef"),
    ORDER("39da3525-afe4-45ff-8977-c53b7b359158"),
    TRANSFER("d3b07384-8d1c-4e6b-9b8e-2f3b8e4a1c9f"),
    TRANSFER_REQUEST("b2c4d5e6-7f8a-4e9b-8c1d-2e3f8e4a3b8f"),
    VISIT_NOTE("d7151f82-c1f3-4152-a605-2f9ea7414a79"),
    VITALS("67a71486-1a54-468f-ac3e-7091a9a79584");

    private final String uuid;
}