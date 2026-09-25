package common.storages;

import apiParts.models.encounter.CreateEncounterResponse;
import apiParts.models.patient.CreatePatientResponse;
import apiParts.models.procedure.ProcedureResponse;
import apiParts.models.visit.CreateVisitResponse;

import java.util.ArrayList;
import java.util.List;

// Preconditions created by extensions within one test (server responses only).
// ThreadLocal - so parallel tests do not share data.
// Numbers (number) start from 1.
public class SessionStorage {
    private static final ThreadLocal<SessionStorage> INSTANCE = ThreadLocal.withInitial(SessionStorage::new);

    private final List<CreatePatientResponse> patients = new ArrayList<>();
    private final List<ProcedureResponse> procedures = new ArrayList<>();
    private final List<CreateEncounterResponse> orderEncounters = new ArrayList<>();
    private final List<CreateEncounterResponse> encounters = new ArrayList<>();
    private final List<CreateVisitResponse> visits = new ArrayList<>();

    private SessionStorage() {
    }

    // ======== PATIENTS (@CreatePatient) ========
    public static void addPatients(List<CreatePatientResponse> patients) {
        INSTANCE.get().patients.addAll(patients);
    }

    public static CreatePatientResponse getPatient() {
        return getPatient(1);
    }

    public static CreatePatientResponse getPatient(int number) {
        return get(INSTANCE.get().patients, number, "patients", "@CreatePatient");
    }

    public static List<CreatePatientResponse> getPatients() {
        return List.copyOf(INSTANCE.get().patients);
    }

    public static void clearPatients() {
        INSTANCE.get().patients.clear();
    }

    // ======== PROCEDURES (@CreateProcedure) ========
    public static void addProcedure(ProcedureResponse procedure) {
        INSTANCE.get().procedures.add(procedure);
    }

    public static ProcedureResponse getProcedure() {
        return getProcedure(1);
    }

    public static ProcedureResponse getProcedure(int number) {
        return get(INSTANCE.get().procedures, number, "procedures", "@CreateProcedure");
    }

    public static void clearProcedures() {
        INSTANCE.get().procedures.clear();
    }

    // ======== ORDERS (@CreateOrder) ========
    public static void addOrderEncounter(CreateEncounterResponse encounter) {
        INSTANCE.get().orderEncounters.add(encounter);
    }

    public static CreateEncounterResponse getOrderEncounter() {
        return get(INSTANCE.get().orderEncounters, 1, "orders", "@CreateOrder");
    }

    // uuid of the order from order encounter (encounter contains one order)
    public static String getOrderUuid() {
        return getOrderEncounter().getOrders().get(0).getUuid();
    }

    public static void clearOrders() {
        INSTANCE.get().orderEncounters.clear();
    }

    // ======== ENCOUNTERS (@CreateEncounter) ========
    public static void addEncounter(CreateEncounterResponse encounter) {
        INSTANCE.get().encounters.add(encounter);
    }

    public static CreateEncounterResponse getEncounter() {
        return getEncounter(1);
    }

    public static CreateEncounterResponse getEncounter(int number) {
        return get(INSTANCE.get().encounters, number, "encounters", "@CreateEncounter");
    }

    public static List<CreateEncounterResponse> getEncounters() {
        return List.copyOf(INSTANCE.get().encounters);
    }

    public static void clearEncounters() {
        INSTANCE.get().encounters.clear();
    }
    // ======== VISITS (@CreateVisit) ========
    public static void addVisit(CreateVisitResponse visit) {
        INSTANCE.get().visits.add(visit);
    }

    public static CreateVisitResponse getVisit() {
        return getVisit(1);
    }

    public static CreateVisitResponse getVisit(int number) {
        return get(INSTANCE.get().visits, number, "visits", "@CreateVisit");
    }

    public static List<CreateVisitResponse> getVisits() {
        return List.copyOf(INSTANCE.get().visits);
    }

    public static void clearVisits() {
        INSTANCE.get().visits.clear();
    }
    // ======== HELPERS ========
    private static <T> T get(List<T> items, int number, String name, String annotation) {
        if (items.isEmpty()) {
            throw new IllegalStateException("No " + name + " in SessionStorage - mark test class or method with " + annotation);
        }
        if (number < 1 || number > items.size()) {
            throw new IllegalArgumentException(
                    "Number of " + name + " must be in 1.." + items.size() + ", but was " + number);
        }
        return items.get(number - 1);
    }
}
