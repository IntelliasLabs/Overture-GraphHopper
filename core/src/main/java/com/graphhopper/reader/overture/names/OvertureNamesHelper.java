package com.graphhopper.reader.overture.names;

/**
 * Utility class for operations on OvertureNames.
 */
public final class OvertureNamesHelper {

    private OvertureNamesHelper() {}

    /**
     * Gets the road name for a specific language.
     *
     * @param names        the OvertureNames instance
     * @param languageCode IETF BCP-47 language tag (e.g., "en", "de", "zh-Hans")
     * @return the localized name if available, otherwise falls back to primary
     */
    public static String getNameForLanguage(OvertureNames names, String languageCode) {
        if (names == null) {
            return null;
        }
        if (names.getCommon() == null || languageCode == null) {
            return names.getPrimary();
        }
        Bcp47LanguageTag tag = Bcp47LanguageTag.parse(languageCode);
        if (tag == null) {
            return names.getPrimary();
        }
        String name = names.getCommon().get(tag);
        return name != null ? name : names.getPrimary();
    }
}
