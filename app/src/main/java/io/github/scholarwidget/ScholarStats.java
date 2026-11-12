package io.github.scholarwidget;

/** One snapshot of a Google Scholar profile's citation statistics. */
public final class ScholarStats {
    public String name = "";
    public int citationsAll;
    public int citationsSince;
    public int hAll;
    public int hSince;
    public int i10All;
    public int i10Since;
    /** The year in Scholar's "Since 20xx" column, or 0 if it was not found. */
    public int sinceYear;
    /** Citations-per-year chart; both arrays have the same length (possibly 0). */
    public int[] years = new int[0];
    public int[] counts = new int[0];
    /** System.currentTimeMillis() when this snapshot was fetched. */
    public long fetchedAt;
}
