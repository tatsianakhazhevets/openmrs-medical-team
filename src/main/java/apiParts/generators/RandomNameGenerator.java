package apiParts.generators;


public class RandomNameGenerator {
    private static final String NAME_REGEX = "[A-Za-z]{2,50}";

    public static String generateNonExistingDisplayName() {
        String givenName = RandomModelGenerator.randomWord(NAME_REGEX);
        String familyName = RandomModelGenerator.randomWord(NAME_REGEX);

        return givenName + " " + familyName;
    }

    public static String generateNonExistingName() {
        String name = RandomModelGenerator.randomWord(NAME_REGEX);
         return name;
    }
}