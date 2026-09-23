package apiParts.assertions;

import apiParts.models.encounter.Ref;
import apiParts.models.visit.CreateVisitRequest;
import apiParts.models.visit.GetVisitResponse;
import apiParts.models.visit.VisitLocation;
import apiParts.models.visit.VisitType;

/**
 * Builds expected visit from request for {@link ModelAssertions#assertMatchesExpected}.
 */
public class VisitAssertions {

    private VisitAssertions() {
    }

    public static GetVisitResponse expectedVisitOf(CreateVisitRequest request) {
        GetVisitResponse expected = new GetVisitResponse();

        expected.setPatient(ref(request.getPatient()));
        expected.setVisitType(ref(request.getVisitType()));
        expected.setLocation(ref(request.getLocation()));
        expected.setStartDatetime(request.getStartDatetime());
        expected.setStopDatetime(request.getStopDatetime());

        return expected;
    }

    private static Ref ref(String uuid) {
        return uuid == null ? null : Ref.of(uuid);
    }

    private static Ref ref(VisitType value) {
        return value == null ? null : Ref.of(value.getUuid());
    }

    private static Ref ref(VisitLocation value) {
        return value == null ? null : Ref.of(value.getUuid());
    }
}