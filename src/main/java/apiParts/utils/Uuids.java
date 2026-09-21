package apiParts.utils;

import apiParts.models.HasUuid;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

public final class Uuids {

    private Uuids() {
    }

    // uuids of models / refs as a sorted set: order of search results does not matter,
    // e.g. Uuids.of(encounter.getObs()) vs Uuids.of(patientObs.results())
    public static Set<String> of(Collection<? extends HasUuid> models) {
        return models.stream()
                .map(HasUuid::getUuid)
                .collect(Collectors.toCollection(TreeSet::new));
    }
}
