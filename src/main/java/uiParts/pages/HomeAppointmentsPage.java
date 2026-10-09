package uiParts.pages;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import static com.codeborne.selenide.Condition.exactText;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selectors.byText;
import static com.codeborne.selenide.Selenide.*;

public class HomeAppointmentsPage extends BasePage<HomeAppointmentsPage> {
    private final SelenideElement createAppointmentButton = $x("//button[normalize-space()='Create new appointment']");
    private final SelenideElement searchInput = $("[data-testid='patientSearchBar']");
    private final SelenideElement searchButton =
            $x("//button[@type='submit' and normalize-space()='Search']");
    private final SelenideElement searchResults = $("[data-openmrs-role='Search Results']");
    private final SelenideElement patientBanner = $("[data-extension-id='patient-banner']");
    private final SelenideElement locationDropdown = $("#location");
    private final SelenideElement serviceDropdown = $("#service");
    private final SelenideElement appointmentTypeDropdown = $("#appointmentType");
    private final SelenideElement duration = $("#duration");
    private final SelenideElement note = $("#appointmentNote");
    private final SelenideElement appointmentDatePicker = $("#datePickerInput");
    private final SelenideElement issuedDatePicker = $("#dateAppointmentScheduledPickerInput");
    private final SelenideElement saveButton = $x("//button[normalize-space()='Save and close']");
    private final SelenideElement discardButton = $x("//button[normalize-space()='Discard']");
    private final SelenideElement scheduleDatePicker = $("#appointment-date-picker");
    private final SelenideElement searchResultsHeader =
            $$("h2").findBy(exactText("0 search results"));
    private final SelenideElement emptySearchResultsMessage =
            $(byText("Sorry, no patient charts were found"));
    private final SelenideElement serviceError = $("#service-error-msg");
    private final SelenideElement durationError = $("#duration-error-msg");



    @Override
    public String url() {
        return "/home/appointments";
    }


    public HomeAppointmentsPage searchPatient(String name) {
        searchInput.setValue(name);
        searchButton.click();
        return this;
    }

    public HomeAppointmentsPage clickPatientCard(String name) {
        searchResults
                .$x(".//button[.//span[normalize-space()='" + name + "']]")
                .shouldBe(visible)
                .click();
        return this;
    }

    public HomeAppointmentsPage shouldHavePatientName(String name) {
        patientBanner.$x(".//span[normalize-space()='" + name + "']")
                .shouldBe(Condition.visible);
        return this;
    }

    public HomeAppointmentsPage selectLocation(String location) {
        locationDropdown.selectOption(location);
        return this;
    }

    public HomeAppointmentsPage selectService(String service) {
        serviceDropdown.selectOption(service);
        return this;
    }

    public HomeAppointmentsPage selectAppointmentType(String appointmentType) {
        appointmentTypeDropdown.selectOption(appointmentType);
        return this;
    }

    public HomeAppointmentsPage setDuration(int minutes) {
        duration.setValue(String.valueOf(minutes));
        return this;
    }

    private void selectDate(SelenideElement datePicker, LocalDate date) {
        SelenideElement calendarButton =
                datePicker.$("button[aria-label='Calendar']");

        calendarButton.scrollIntoView(true).shouldBe(Condition.visible).click();

        SelenideElement calendar = $("[role='application'][aria-label]")
                .shouldBe(Condition.visible);

        String dateLabel = date.format(
                DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.ENGLISH));

        calendar.$x(
                        ".//*[@role='button' and contains(@aria-label, '"
                                + dateLabel + "')]")
                .scrollIntoView(true)
                .shouldBe(Condition.visible)
                .click();
    }

    public HomeAppointmentsPage selectAppointmentDate(LocalDate date) {
        selectDate(appointmentDatePicker, date);
        return this;
    }

    public HomeAppointmentsPage selectIssuedDate(LocalDate date) {
        selectDate(issuedDatePicker, date);
        return this;
    }

    public HomeAppointmentsPage selectScheduleDate(LocalDate date) {
        selectDate(scheduleDatePicker, date);
        return this;
    }

    public HomeAppointmentsPage shouldHaveAppointmentDate(LocalDate date) {
        appointmentDatePicker.$("input[type='text']").shouldHave(Condition.value(date.toString()));
        return this;
    }

    public HomeAppointmentsPage shouldHaveIssuedDate(LocalDate date) {
        issuedDatePicker.$("input[type='text']").shouldHave(Condition.value(date.toString()));
        return this;
    }

    public HomeAppointmentsPage clickCreateAppointment() {
        createAppointmentButton.shouldBe(visible).click();
        return this;
    }

    public HomeAppointmentsPage saveAppointment() {
        saveButton.shouldBe(visible).click();
        return this;
    }

    public HomeAppointmentsPage discardAppointment() {
        discardButton.shouldBe(visible).click();
        return this;
    }

    public HomeAppointmentsPage setNote(String text) {
        note.setValue(text);
        return this;
    }
    private SelenideElement appointmentRow(String patientName) {
        return $x("//tr[@data-parent-row='true' and .//a[normalize-space()='"
                + patientName + "']]");
    }

    public HomeAppointmentsPage shouldHaveAppointment(
            String patientName,
            String identifier,
            String location,
            String service,
            String status
    ) {
        SelenideElement row = appointmentRow(patientName)
                .shouldBe(Condition.visible);

        ElementsCollection cells = row.$$("td");

        cells.get(2).shouldHave(exactText(patientName));
        cells.get(3).shouldHave(exactText(identifier));
        cells.get(4).shouldHave(exactText(location));
        cells.get(5).shouldHave(exactText(service));
        cells.get(8).shouldHave(exactText(status));

        return this;
    }
    public HomeAppointmentsPage shouldShowNoSearchResults() {
        searchResultsHeader.shouldHave(exactText("0 search results"));
        emptySearchResultsMessage.shouldHave(exactText("Sorry, no patient charts were found"));
        return this;
    }
    public HomeAppointmentsPage shouldShowServiceErrorMessage() {
        serviceError.shouldHave(exactText("Service is required"));
        return this;
    }
    public HomeAppointmentsPage shouldShowDurationErrorMessage() {
        durationError.shouldHave(exactText("Duration should be greater than zero"));
        return this;
    }

}
