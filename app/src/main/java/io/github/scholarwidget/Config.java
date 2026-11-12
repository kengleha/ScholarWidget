package io.github.scholarwidget;

/** The only things you should need to edit. */
public final class Config {
    private Config() {}

    /** The "user=" value from the Google Scholar profile URL. */
    public static final String USER_ID = "WkN17coAAAAJ";

    public static final String PROFILE_URL =
            "https://scholar.google.com/citations?hl=en&user=" + USER_ID;

    /**
     * How often the widget refreshes in the background. Scholar updates its counts
     * every few days and blocks clients that ask too often, so keep this long.
     */
    public static final int REFRESH_HOURS = 12;
}
