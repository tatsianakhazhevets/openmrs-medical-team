package apiParts.models.errors;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

// DrugOrderValidator errors, see project doc claude/drug-order-validation.md
@Getter
@RequiredArgsConstructor
public enum DrugOrderFieldError implements FieldError {
    // Simple dosing
    DOSE_ZERO_OR_LESS("dose", "DrugOrder.error.doseZeroOrLess"),
    DOSE_IS_NULL_FOR_SIMPLE_DOSING("dose", "DrugOrder.error.doseIsNullForDosingTypeSimple"),
    ROUTE_IS_NULL_FOR_SIMPLE_DOSING("route", "DrugOrder.error.routeIsNullForDosingTypeSimple"),
    DOSE_UNITS_NOT_AMONG_ALLOWED("doseUnits", "DrugOrder.error.notAmongAllowedConcepts"),

    // Free text dosing
    DOSING_INSTRUCTIONS_IS_NULL_FOR_FREE_TEXT_DOSING("dosingInstructions", "DrugOrder.error.dosingInstructionsIsNullForDosingTypeFreeText"),

    // Outpatient (drugOrder.requireOutpatientQuantity = true)
    QUANTITY_IS_NULL_FOR_OUTPATIENT("quantity", "DrugOrder.error.quantityIsNullForOutPatient"),
    QUANTITY_UNITS_REQUIRED_WITH_QUANTITY("quantityUnits", "DrugOrder.error.quantityUnitsRequiredWithQuantity"),
    NUM_REFILLS_IS_NULL_FOR_OUTPATIENT("numRefills", "DrugOrder.error.numRefillsIsNullForOutPatient"),

    // Duration
    DURATION_UNITS_REQUIRED_WITH_DURATION("durationUnits", "DrugOrder.error.durationUnitsRequiredWithDuration"),

    // Drug and concept (server also returns drug: error.general - generic code, not checked)
    CONCEPT_NOT_MATCHING_DRUG("concept", "error.concept"),

    // KNOWN ISSUES: server accepts these values (201), expected error code is unknown
    QUANTITY_ZERO_OR_LESS("quantity", null),
    NUM_REFILLS_NEGATIVE("numRefills", null);

    private final String field;
    private final String code;
}
