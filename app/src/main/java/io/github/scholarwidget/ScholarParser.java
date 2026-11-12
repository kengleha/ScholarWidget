package io.github.scholarwidget;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extracts the statistics from the HTML of a Google Scholar profile page.
 * Pure Java (no Android classes) so it can be unit-tested on a desktop JVM.
 *
 * Scholar has no API, so this depends on the page's markup: the stats table
 * cells carry class "gsc_rsb_std" in the order citations(all, since),
 * h-index(all, since), i10-index(all, since). If Google changes the markup,
 * this is the file to update.
 */
public final class ScholarParser {
    private ScholarParser() {}

    private static final Pattern STAT_CELL =
            Pattern.compile("<td[^>]*class=\"gsc_rsb_std\"[^>]*>\\s*(\\d+)\\s*</td>");
    private static final Pattern NAME =
            Pattern.compile("id=\"gsc_prf_in\"[^>]*>([^<]+)<");
    private static final Pattern SINCE = Pattern.compile("Since\\s+(\\d{4})");
    private static final Pattern CHART_YEAR =
            Pattern.compile("class=\"gsc_g_t\"[^>]*>\\s*(\\d{4})\\s*<");
    // Each bar: <a class="gsc_g_a" style="...;z-index:N"><span class="gsc_g_al">COUNT</span></a>
    // N counts bars from the right (1 = most recent year). Years with zero
    // citations have no bar, so the z-index is what ties a bar to its year.
    private static final Pattern CHART_BAR =
            Pattern.compile("z-index:\\s*(\\d+)[^>]*>\\s*<span[^>]*class=\"gsc_g_al\"[^>]*>\\s*(\\d+)\\s*<");

    public static ScholarStats parse(String html) throws IOException {
        if (html == null || html.isEmpty()) {
            throw new IOException("Empty response");
        }

        List<Integer> cells = new ArrayList<>();
        Matcher m = STAT_CELL.matcher(html);
        while (m.find()) {
            cells.add(Integer.parseInt(m.group(1)));
        }
        if (cells.size() < 6) {
            if (html.contains("gs_captcha") || html.contains("unusual traffic")
                    || html.contains("recaptcha")) {
                throw new IOException("Scholar asked for a CAPTCHA");
            }
            throw new IOException("Stats table not found");
        }

        ScholarStats s = new ScholarStats();
        s.citationsAll = cells.get(0);
        s.citationsSince = cells.get(1);
        s.hAll = cells.get(2);
        s.hSince = cells.get(3);
        s.i10All = cells.get(4);
        s.i10Since = cells.get(5);

        m = NAME.matcher(html);
        if (m.find()) {
            s.name = unescape(m.group(1).trim());
        }

        m = SINCE.matcher(html);
        if (m.find()) {
            s.sinceYear = Integer.parseInt(m.group(1));
        }

        List<Integer> years = new ArrayList<>();
        m = CHART_YEAR.matcher(html);
        while (m.find()) {
            years.add(Integer.parseInt(m.group(1)));
        }
        int n = years.size();
        if (n > 0) {
            s.years = new int[n];
            s.counts = new int[n];
            for (int i = 0; i < n; i++) {
                s.years[i] = years.get(i);
            }
            m = CHART_BAR.matcher(html);
            while (m.find()) {
                int fromRight = Integer.parseInt(m.group(1));
                int index = n - fromRight;
                if (index >= 0 && index < n) {
                    s.counts[index] = Integer.parseInt(m.group(2));
                }
            }
        }
        return s;
    }

    private static String unescape(String t) {
        return t.replace("&amp;", "&")
                .replace("&#39;", "'")
                .replace("&quot;", "\"")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&nbsp;", " ");
    }
}
