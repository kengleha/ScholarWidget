package io.github.scholarwidget;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Turns what the user typed (a profile URL or a bare ID) into a Scholar user ID. */
public final class ProfileId {
    private ProfileId() {}

    private static final Pattern IN_URL = Pattern.compile("[?&]user=([A-Za-z0-9_-]+)");
    private static final Pattern BARE = Pattern.compile("[A-Za-z0-9_-]{8,20}");

    /** Returns the ID, or null if the input is neither a profile URL nor an ID. */
    public static String parse(String input) {
        if (input == null) {
            return null;
        }
        String t = input.trim();
        Matcher m = IN_URL.matcher(t);
        if (m.find()) {
            return m.group(1);
        }
        return BARE.matcher(t).matches() ? t : null;
    }
}
