package io.github.scholarwidget;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

/** Minimal screen: explains how to add the widget and offers a manual refresh. */
public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int pad = (int) (24 * getResources().getDisplayMetrics().density);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(pad, pad, pad, pad);

        TextView text = new TextView(this);
        text.setTextSize(16f);
        text.setText("To add the widget: long-press an empty area of your home screen, "
                + "choose Widgets, and pick \"Scholar Stats\".\n\n"
                + "It refreshes about every " + Config.REFRESH_HOURS + " hours. "
                + "Tap the ↻ icon on the widget to refresh now, or tap the widget "
                + "to open the profile.\n\nProfile: " + Config.PROFILE_URL + "\n");
        root.addView(text);

        Button button = new Button(this);
        button.setText("Refresh now");
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ScholarWidgetProvider.refreshNow(MainActivity.this);
            }
        });
        root.addView(button, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        setContentView(root);
    }
}
