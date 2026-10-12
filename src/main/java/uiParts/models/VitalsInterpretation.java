package uiParts.models;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * How a vitals value relates to the reference range of its concept.
 * Names match obs.interpretation returned by the server (GET /obs?v=full).
 * UI marks the table cell with a background (Carbon orange-10 for abnormal, red-20 for critical)
 * and an arrow in the ::after pseudo element; the text color does not change.
 */
@Getter
@RequiredArgsConstructor
public enum VitalsInterpretation {
    NORMAL("rgba(0, 0, 0, 0)", ""),
    HIGH("rgba(255, 242, 232, 1)", "↑"),
    LOW("rgba(255, 242, 232, 1)", "↓"),
    CRITICALLY_HIGH("rgba(255, 215, 217, 1)", "↑↑"),
    CRITICALLY_LOW("rgba(255, 215, 217, 1)", "↓↓");

    private final String backgroundColor;
    private final String flag;
}
