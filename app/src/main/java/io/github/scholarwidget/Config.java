package io.github.scholarwidget;

/** Build-time defaults. The profile itself is chosen in the app (see MainActivity). */
public final class Config {
    private Config() {}

    /**
     * Profile shown until the user enters one in the app: the "user=" value from a
     * Google Scholar profile URL. Set to "" to start with no profile.
     */
    public static final String DEFAULT_USER_ID = "WkN17coAAAAJ";

    /**
     * How often the widget refreshes in the background. Scholar updates its counts
     * every few days and blocks clients that ask too often, so keep this long.
     */
    public static final int REFRESH_HOURS = 12;

    public static String profileUrl(String userId) {
        return "https://scholar.google.com/citations?hl=en&user=" + userId;
    }
}
