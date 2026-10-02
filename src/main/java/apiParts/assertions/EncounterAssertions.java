package apiParts.assertions;

import apiParts.models.Ref;
import apiParts.models.encounter.EncounterResponse;
import apiParts.models.visit.GetVisitResponse;

import java.util.List;
import java.util.function.Function;

public final class EncounterAssertions {

    private EncounterAssertions() {
    }

    public static void assertCreatedEncounter(EncounterResponse created, int expectedReferenceCount,
                                              Function<EncounterResponse, List<Ref>> references, String description) {
        SoftlyContext.get().assertThat(created.getUuid())
                .as("encounter uuid")
                .isNotBlank();
        SoftlyContext.get().assertThat(references.apply(created))
                .as(description + " saved with encounter")
                .hasSize(expectedReferenceCount);
    }

    public static void assertEncounterPersisted(EncounterResponse created, EncounterResponse saved,
                                                Function<EncounterResponse, List<Ref>> references, String description) {
        SoftlyContext.get().assertThat(saved.getUuid())
                .as("created encounter is retrievable")
                .isEqualTo(created.getUuid());
        SoftlyContext.get().assertThat(references.apply(saved))
                .extracting(Ref::getUuid)
                .as("persisted " + description)
                .containsExactlyInAnyOrderElementsOf(
                        references.apply(created).stream().map(Ref::getUuid).toList());
    }

    public static void assertDifferentEncounterTypesInVisit(GetVisitResponse savedVisit,
                                                              EncounterResponse first, EncounterResponse second) {
        SoftlyContext.get().assertThat(first.getEncounterType().getUuid())
                .as("encounter types are different")
                .isNotEqualTo(second.getEncounterType().getUuid());
        SoftlyContext.get().assertThat(savedVisit.getEncounters())
                .extracting(Ref::getUuid)
                .as("created visit is retrievable with both encounters")
                .containsExactlyInAnyOrder(first.getUuid(), second.getUuid());
    }
}
