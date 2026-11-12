package io.github.scholarwidget;

import android.app.Activity;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

/** Settings screen: choose the Scholar profile the widget shows, and refresh on demand. */
public class MainActivity extends Activity {
    private EditText input;
    private TextView current;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int pad = (int) (24 * getResources().getDisplayMetrics().density);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(pad, pad, pad, pad);

        TextView heading = new TextView(this);
        heading.setTextSize(18f);
        heading.setText("Google Scholar profile");
        root.addView(heading);

        TextView help = new TextView(this);
        help.setTextSize(14f);
        help.setText("Paste the profile's web address (for example "
                + "https://scholar.google.com/citations?user=XXXXXXXXXXXX) "
                + "or just the ID that follows \"user=\".\n");
        root.addView(help);

        input = new EditText(this);
        input.setHint("Profile URL or ID");
        input.setSingleLine(true);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);
        input.setText(StatsStore.getUserId(this));
        root.addView(input, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        Button save = new Button(this);
        save.setText("Save and refresh");
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveProfile();
            }
        });
        root.addView(save, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        current = new TextView(this);
        current.setTextSize(14f);
        current.setPadding(0, pad / 2, 0, pad);
        root.addView(current);

        TextView about = new TextView(this);
        about.setTextSize(14f);
        about.setText("To add the widget: long-press an empty area of your home screen, "
                + "choose Widgets, and pick \"Scholar Stats\".\n\n"
                + "It refreshes about every " + Config.REFRESH_HOURS + " hours. "
                + "Tap the ↻ icon on the widget to refresh now, or tap the widget "
                + "to open the profile.");
        root.addView(about);

        ScrollView scroll = new ScrollView(this);
        scroll.addView(root);
        setContentView(scroll);
        showCurrent();
    }

    private void saveProfile() {
        String id = ProfileId.parse(input.getText().toString());
        if (id == null) {
            Toast.makeText(this, "That doesn't look like a Scholar profile URL or ID",
                    Toast.LENGTH_LONG).show();
            return;
        }
        if (!id.equals(StatsStore.getUserId(this))) {
            StatsStore.setUserId(this, id); // also clears the previous profile's numbers
        }
        input.setText(id);
        ScholarWidgetProvider.refreshNow(this);
        showCurrent();
        Toast.makeText(this, "Saved. The widget will update shortly.", Toast.LENGTH_SHORT).show();
    }

    private void showCurrent() {
        String id = StatsStore.getUserId(this);
        current.setText(id.isEmpty() ? "No profile set." : "Current profile ID: " + id);
    }
}
