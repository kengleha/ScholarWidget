# Scholar Stats widget

An Android home-screen widget showing a Google Scholar profile's citations,
h-index and i10-index (all-time and "since" values), with the citations-per-year
bar chart. Tap ↻ to refresh; tap anywhere else to open the profile.

## Get the APK

**Option A – GitHub (no computer setup needed)**

1. Create a new GitHub repository and upload the contents of this folder.
2. Open the repository's **Actions** tab; the "Build APK" workflow runs on push.
3. When it finishes, download the `ScholarWidget-debug-apk` artifact, unzip it,
   and open `app-debug.apk` on the phone (allow "install unknown apps" when asked).

**Option B – Android Studio**

Open this folder in Android Studio, let it sync, plug in the phone and press Run.

Then long-press the home screen → Widgets → **Scholar Stats**.

## Changing the profile or refresh rate

Edit `app/src/main/java/io/github/scholarwidget/Config.java`.

## How it works, and its limits

- Google Scholar has no API. The widget downloads the public profile page from
  the phone and reads the stats table (`ScholarParser.java`). If Google changes
  the page markup, that parser is the one file to update.
- Scholar rate-limits automated requests. The widget asks only every 12 hours
  and keeps showing the last good numbers if a refresh fails; the status line
  says when a refresh was blocked.
- Android decides exactly when background jobs run, so "every 12 hours" is
  approximate, especially under battery saver.

## Layout

| File | Purpose |
| --- | --- |
| `Config.java` | Profile ID and refresh interval |
| `ScholarFetcher.java` / `ScholarParser.java` | Download and parse the page |
| `StatsStore.java` | Last snapshot, in SharedPreferences |
| `RefreshJobService.java` | Background refresh via JobScheduler |
| `ScholarWidgetProvider.java` | Draws the widget |
| `ChartRenderer.java` | Citations-per-year bar chart |
