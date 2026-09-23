package apiParts.generators;

/**
 * Scenario a model is generated for, see {@link RandomModelGenerator#generate(Class, GenerationProfile)}.
 * Generating rules with {@code profiles} apply only in the listed profiles,
 * rules without {@code profiles} apply always.
 * E.g. CreateEncounterRequest: VITALS -> vitals obs, no orders; ORDER -> orders, no obs.
 */
public enum GenerationProfile {
    VITALS,
    ORDER
}
