package apiParts.models;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Location {
    OUTPATIENT_CLINIC(
            "44c3efb0-2583-4c80-a79e-1f756a03c0a1",
            "Outpatient Clinic"),

    INPATIENT_WARD(
            "ba685651-ed3b-4e63-9b35-78893060758a",
            "Inpatient Ward"),

    COMMUNITY_OUTREACH(
            "1ce1b7d4-c865-4178-82b0-5932e51503d6",
                    "Community Outreach"),

    MAIN_PHARMACY(
            "7f65d926-57d6-4402-ae10-a5b3bcbf7986",
                    "Main Pharmacy"),

    MAIN_STORE(
            "19dbe2c0-289d-11ed-bdcb-507b9dea1806",
                    "Main Store"),

    MOBILE_CLINIC(
            "8d9045ad-50f0-45b8-93c8-3ed4bce19dbf",
                    "Mobile Clinic"),

    SITE_1(
            "dbdaabf6-a326-4804-aba7-062073e05cd1",
                    "Site 1"),

    SITE_2(
            "4a06a96f-dd21-4695-8d76-b9c405cebafc",
                    "Site 2"),

    SITE_3(
            "d493f3eb-ba7c-4e6c-988d-62491e496045",
                    "Site 3"),

    SITE_4(
            "dd2ad4cd-e33a-40cc-955a-e91e313f9a3b",
                    "Site 4"),

    SITE_10(
            "5f47d469-7c8d-4f34-887c-16db797bec3d",
                    "Site 10"),

    SITE_11(
            "d81f9408-1582-4270-848f-6af319424733",
                    "Site 11"),

    SITE_12(
            "ffd78808-5b88-4a56-9c94-54754725ad90",
                    "Site 12"),

    SITE_13(
            "a61a5145-8da1-4410-be23-479314814cc7",
                    "Site 13"),

    SITE_14(
            "5e6abb9d-7ab3-4203-8b02-1462bcca0c51",
                    "Site 14"),

    SITE_15(
            "6536f254-a296-4d0f-84f7-9c0f0b12536e",
                    "Site 15"),

    SITE_16(
            "0cb740b2-153f-49e2-b599-aabb90330bd2",
                    "Site 16"),

    SITE_17(
            "2cead8f3-0e6c-468b-9f10-6a7ca75604dd",
                    "Site 17"),

    SITE_18(
            "facac3e6-8c4a-4528-a497-8cf22ffd1a50",
                    "Site 18"),

    SITE_19(
            "29fc4038-18d4-4b11-bc2b-63983932e6ac",
                    "Site 19"),

    SITE_20(
            "c84338a5-69a7-411a-aa0f-f405cb44697b",
                    "Site 20"),

    SITE_21(
            "09d08c88-5b12-4831-8268-62242822180d",
                    "Site 21"),

    SITE_22(
            "ac529390-16d6-4454-83be-4fcbbc5988aa",
                    "Site 22"),

    SITE_23(
            "b03c3107-fc5b-4b49-a31d-02bf327d6dd4",
                    "Site 23"),

    SITE_24(
            "302090a6-11ac-455d-80c9-08c06ebd1087",
                    "Site 24"),

    SITE_25(
            "bb5787d4-307e-40ec-89b3-f9f539a5d88f",
                    "Site 25"),

    SITE_26(
            "13ecc2c0-7170-4d51-a93d-7189fd885c05",
                    "Site 26"),

    SITE_27(
            "6dfacc57-17e6-481e-bde6-36742bf2bb66",
                    "Site 27"),

    SITE_28(
            "2425adbe-b446-4d70-a684-5d757370a230",
                    "Site 28"),

    SITE_29(
            "d114a8ac-aa7e-4350-a8bf-de2caf0ea440",
                    "Site 29"),

    SITE_30(
            "ca22282f-0d68-4c1c-a985-90b69cf943a3",
                    "Site 30"),

    SITE_31(
            "8d976ed0-49fe-4ed9-909d-f7b9d4081712",
                    "Site 31"),

    SITE_32(
            "370c2cff-4681-40fa-bd29-b557399a6486",
                    "Site 32"),

    SITE_33(
            "3ad56ffe-05bc-4ccf-aa87-f517f65e10fd",
                    "Site 33"),

    SITE_34(
            "d38b9028-8035-4595-84ca-c8e7c9e2ecd4",
                    "Site 34"),

    SITE_35(
            "07032097-5031-4c93-a29a-52a14d124a0a",
                    "Site 35"),

    SITE_36(
            "aaf478f9-b3e2-4038-977b-56896a8060e2",
                    "Site 36"),

    SITE_37(
            "9a93ba80-8a44-4f99-980c-b1e13dbca33a",
                    "Site 37"),

    SITE_38(
            "5c0ff819-e936-4e75-9c1d-4d001a6fd95e",
                    "Site 38"),

    SITE_39(
            "743a7d4a-3404-49db-a0ec-beddbdec0919",
                    "Site 39"),

    SITE_40(
            "1b1317ae-3688-416d-a8f0-1378b92b9e04",
                    "Site 40"),

    SITE_41(
            "c2861e47-ecc1-4e81-992d-7b3c29fce0b0",
                    "Site 41"),

    SITE_42(
            "92dbdbdf-17da-4cf0-873c-ad15dfae71cb",
                    "Site 42"),

    SITE_43(
            "fd7fae67-7a2e-456c-81db-3cbdb10fd510",
                    "Site 43"),

    SITE_44(
            "2ccae22b-26ab-4c40-a813-55462e27a0c8",
                    "Site 44"),

    SITE_45(
            "2ef7caf2-affa-4003-8fe7-128db6ce31ee",
                    "Site 45"),

    SITE_46(
            "148dc112-9e26-42f9-b514-e28cf2e44b1f",
                    "Site 46"),

    SITE_47(
            "6d49188b-2bdf-4c6e-bdff-7eeed3e15a64",
                    "Site 47"),

    SITE_48(
            "0fa578fc-301a-418c-9cf9-b35707fcb478",
                    "Site 48"),

    SITE_49(
            "eeba7391-361a-41ad-a884-02d5504abaac",
                    "Site 49");

    @JsonValue
    private final String uuid;
    private final String display;
}