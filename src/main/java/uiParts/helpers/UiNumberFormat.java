package uiParts.helpers;

import java.math.BigDecimal;

public final class UiNumberFormat {

    private UiNumberFormat() {
    }

    // Number as the UI shows it: no trailing zeros (37.0 -> "37", 70.5 -> "70.5")
    public static String format(Number value) {
        return new BigDecimal(String.valueOf(value)).stripTrailingZeros().toPlainString();
    }
}
