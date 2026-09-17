package apiParts.models.errors;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

// Order business rule errors without fieldErrors: {"error": {"message": "[<message>]"}}
@Getter
@RequiredArgsConstructor
public enum OrderErrorMessage {
    // second active order for the same drug and patient (KNOWN ISSUE: server returns 500 instead of 400)
    MORE_THAN_ONE_ACTIVE_ORDER("Order.cannot.have.more.than.one"),

    // careSetting missing from order (KNOWN ISSUE: server returns 500 instead of 400 with a careSetting field error)
    CARE_SETTING_CANNOT_BE_DETERMINED("Order.care.cannot.determine");

    private final String message;
}
