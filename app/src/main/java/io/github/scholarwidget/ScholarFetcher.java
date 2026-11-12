package io.github.scholarwidget;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/** Downloads the profile page and parses it. Must be called off the main thread. */
public final class ScholarFetcher {
    private ScholarFetcher() {}

    private static final String USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) "
                    + "Chrome/130.0.0.0 Safari/537.36";

    public static ScholarStats fetch(String userId) throws IOException {
        HttpURLConnection c =
                (HttpURLConnection) new URL(Config.profileUrl(userId)).openConnection();
        try {
            c.setConnectTimeout(15000);
            c.setReadTimeout(20000);
            c.setInstanceFollowRedirects(true);
            c.setRequestProperty("User-Agent", USER_AGENT);
            c.setRequestProperty("Accept", "text/html,application/xhtml+xml");
            c.setRequestProperty("Accept-Language", "en-US,en;q=0.9");

            int code = c.getResponseCode();
            if (code == 429 || code == 403) {
                throw new IOException("Scholar is rate-limiting (HTTP " + code + ")");
            }
            if (code == 404) {
                throw new IOException("Profile not found");
            }
            if (code != 200) {
                throw new IOException("HTTP " + code);
            }

            String html;
            try (InputStream in = c.getInputStream()) {
                html = new String(readAll(in), charsetOf(c.getContentType()));
            }
            ScholarStats stats = ScholarParser.parse(html);
            stats.fetchedAt = System.currentTimeMillis();
            return stats;
        } finally {
            c.disconnect();
        }
    }

    private static byte[] readAll(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream(1 << 16);
        byte[] buf = new byte[8192];
        int n;
        while ((n = in.read(buf)) != -1) {
            out.write(buf, 0, n);
        }
        return out.toByteArray();
    }

    private static Charset charsetOf(String contentType) {
        if (contentType != null) {
            int i = contentType.toLowerCase().indexOf("charset=");
            if (i >= 0) {
                String name = contentType.substring(i + 8).trim();
                int semi = name.indexOf(';');
                if (semi >= 0) {
                    name = name.substring(0, semi);
                }
                try {
                    return Charset.forName(name.replace("\"", "").trim());
                } catch (Exception ignored) {
                    // fall through to UTF-8
                }
            }
        }
        return StandardCharsets.UTF_8;
    }
}
