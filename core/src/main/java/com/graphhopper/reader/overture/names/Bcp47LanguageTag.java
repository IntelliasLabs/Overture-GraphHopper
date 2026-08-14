package com.graphhopper.reader.overture.names;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents an IETF BCP-47 language tag with its parsed components.
 * <p>
 * A BCP-47 tag identifies a language or a locale. This class parses the tag into its
 * standard subtags as defined by RFC 5646.
 * <p>
 * Components (in order):
 * <ol>
 * <li><b>Language:</b> The primary language subtag (required).</li>
 * <li><b>Script:</b> The script subtag (optional).</li>
 * <li><b>Region:</b> The region subtag (optional).</li>
 * <li><b>Variants:</b> Variant subtags (optional).</li>
 * <li><b>Extensions:</b> Extension subtags (optional).</li>
 * </ol>
 */
public class Bcp47LanguageTag {

    /**
     * Regular expression to validate and parse BCP-47 tags.
     * <p>
     * Regex groups:
     * <ol>
     * <li>Language: 2-3 chars (with optional extlangs) OR 4-8 chars.</li>
     * <li>Script: 4 chars.</li>
     * <li>Region: 2 chars or 3 digits.</li>
     * <li>Variants: 5-8 alphanum OR digit+3 alphanum.</li>
     * <li>Extensions: singleton char + 1 or more subtags.</li>
     * </ol>
     */
    private static final Pattern BCP47_PATTERN = Pattern.compile(
            "^((?:[A-Za-z]{2,3}(?:-[A-Za-z]{3}){0,3}?)|(?:[A-Za-z]{4,8}))" + // Group 1: Language
                    "(?:-([A-Za-z]{4}))?"
                    + // Group 2: Script
                    "(?:-([A-Za-z]{2}|[0-9]{3}))?"
                    + // Group 3: Region
                    "((?:-(?:[A-Za-z0-9]{5,8}|[0-9][A-Za-z0-9]{3}))*)"
                    + // Group 4: Variants
                    "((?:-[A-WY-Za-wy-z0-9](?:-[A-Za-z0-9]{2,8})+)*)$" // Group 5: Extensions
            );

    private final String language;
    private final String script;
    private final String region;
    private final List<String> variants;
    private final List<String> extensions;

    /**
     * Private constructor used by the {@link #parse(String)} factory method.
     *
     * @param language   the primary language subtag
     * @param script     the script subtag
     * @param region     the region subtag
     * @param variants   list of variant subtags
     * @param extensions list of extension subtags
     */
    private Bcp47LanguageTag(
            String language,
            String script,
            String region,
            List<String> variants,
            List<String> extensions) {
        this.language = language;
        this.script = script;
        this.region = region;
        this.variants = variants;
        this.extensions = extensions;
    }

    /**
     * Parses a string representation of a BCP-47 language tag into a {@code Bcp47LanguageTag} object.
     *
     * @param tag the language tag string to parse (e.g., "en-US", "zh-Hans-CN")
     * @return the parsed {@code Bcp47LanguageTag}, or {@code null} if the tag is null, empty, or invalid
     */
    public static Bcp47LanguageTag parse(String tag) {
        if (tag == null || tag.isEmpty()) {
            return null;
        }
        Matcher matcher = BCP47_PATTERN.matcher(tag);
        if (!matcher.matches()) {
            return null;
        }

        return new Bcp47LanguageTag(
                matcher.group(1),
                matcher.group(2),
                matcher.group(3),
                parseSubtags(matcher.group(4)),
                parseSubtags(matcher.group(5)));
    }

    /**
     * Helper method to split a concatenated string of subtags (from regex groups) into a list.
     * <p>
     * For example, "-rozaj-biske" becomes ["rozaj", "biske"].
     *
     * @param group the regex group string containing subtags including the leading hyphen
     * @return a list of individual subtags, or an empty list if the group is null/empty
     */
    private static List<String> parseSubtags(String group) {
        if (group == null || group.isEmpty()) {
            return List.of();
        }
        return Arrays.asList(group.substring(1).split("-"));
    }

    /**
     * Reconstructs the full BCP-47 language tag string from its components.
     *
     * @return the complete language tag (e.g., "en-Latn-US")
     */
    public String getTag() {
        StringBuilder sb = new StringBuilder(language);
        if (script != null) sb.append("-").append(script);
        if (region != null) sb.append("-").append(region);
        for (String v : variants) sb.append("-").append(v);
        for (String e : extensions) sb.append("-").append(e);
        return sb.toString();
    }

    /**
     * Gets the primary language subtag.
     *
     * @return the language code (e.g., "en")
     */
    public String getLanguage() {
        return language;
    }

    /**
     * Gets the script subtag.
     *
     * @return the script code (e.g., "Latn"), or null if not present
     */
    public String getScript() {
        return script;
    }

    /**
     * Gets the region subtag.
     *
     * @return the region code (e.g., "US"), or null if not present
     */
    public String getRegion() {
        return region;
    }

    /**
     * Gets the list of variant subtags.
     *
     * @return a list of variants, or an empty list if none exist
     */
    public List<String> getVariants() {
        return variants;
    }

    /**
     * Gets the list of extension subtags.
     *
     * @return a list of extensions, or an empty list if none exist
     */
    public List<String> getExtensions() {
        return extensions;
    }

    /**
     * Returns the string representation of the language tag.
     *
     * @return the full tag string (same as {@link #getTag()})
     */
    @Override
    public String toString() {
        return getTag();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Bcp47LanguageTag that = (Bcp47LanguageTag) o;
        return Objects.equals(language, that.language)
                && Objects.equals(script, that.script)
                && Objects.equals(region, that.region)
                && Objects.equals(variants, that.variants)
                && Objects.equals(extensions, that.extensions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(language, script, region, variants, extensions);
    }
}
