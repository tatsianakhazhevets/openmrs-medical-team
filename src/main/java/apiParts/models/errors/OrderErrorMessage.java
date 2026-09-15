package apiParts.models.errors;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

// Order business rule errors without fieldErrors: {"error": {"message": "[<message>]"}}
@Getter
@RequiredArgsConstructor
public enum OrderErrorMessage {
    // second active order for the same drug and patient (KNOWN ISSUE: server returns 500 instead of 400)
    MORE_THAN_ONE_ACTIVE_ORDER("Order.cannot.have.more.than.one");

    private final String message;
}
