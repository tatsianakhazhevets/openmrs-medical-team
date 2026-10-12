package uiParts.elements;

import lombok.AllArgsConstructor;
import lombok.Getter;

// Left sidebar items of the patient chart.
// extensionId - data-extension-id of the item div (stable locator), label - visible text.
@Getter
@AllArgsConstructor
public enum PatientChartTab {
    PATIENT_SUMMARY("charts-summary-dashboard", "Patient summary"),
    VITALS_AND_BIOMETRICS("results-summary-dashboard", "Vitals & Biometrics"),
    MEDICATIONS("medications-summary-dashboard", "Medications"),
    ORDERS("patient-orders-summary-dashboard", "Orders"),
    RESULTS("test-results-summary-dashboard", "Results"),
    VISITS("encounters-summary-dashboard", "Visits"),
    ALLERGIES("allergies-summary-dashboard", "Allergies"),
    CONDITIONS("conditions-summary-dashboard", "Conditions"),
    PROCEDURES("procedures-summary-dashboard", "Procedures"),
    IMMUNIZATIONS("immunization-summary-dashboard", "Immunizations"),
    ATTACHMENTS("attachments-results-summary-dashboard", "Attachments"),
    PROGRAMS("programs-summary-dashboard", "Programs"),
    APPOINTMENTS("patient-appointments-summary-dashboard", "Appointments"),
    BILLING_HISTORY("billing-summary-dashboard-link", "Billing history");

    private final String extensionId;
    private final String label;
}
