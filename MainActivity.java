package com.web16.app;

import android.app.Activity;
import android.os.Bundle;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.*;
import android.text.InputType;
import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends Activity {
    private final ArrayList<WebView> screens = new ArrayList<>();
    private EditText numberInput, urlInput;
    private SharedPreferences prefs;
    private LinearLayout gridContainer;
    private int columns = 4;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(Color.rgb(15,20,35));
        getWindow().setNavigationBarColor(Color.rgb(15,20,35));
        prefs = getSharedPreferences("web16_links", MODE_PRIVATE);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.rgb(15,20,35));

        LinearLayout bar = new LinearLayout(this);
        bar.setGravity(Gravity.CENTER_VERTICAL);
        bar.setPadding(5, 2, 5, 2);
        bar.setBackgroundColor(Color.rgb(24,32,52));

        TextView title = new TextView(this);
        title.setText("WEB16");
        title.setTextColor(Color.WHITE);
        title.setTextSize(17);
        title.setTypeface(null, Typeface.BOLD);
        title.setPadding(8, 0, 8, 0);
        bar.addView(title);

        Button layoutButton = new Button(this);
        layoutButton.setText("4 × 4");
        bar.addView(layoutButton);
        layoutButton.setOnClickListener(v -> {
            columns = columns == 4 ? 2 : 4;
            layoutButton.setText(columns + " columns");
            buildGrid();
        });

        Button reload = new Button(this);
        reload.setText("Reload");
        bar.addView(reload);
        reload.setOnClickListener(v -> { for (WebView w : screens) w.reload(); });
        root.addView(bar);

        ScrollView scroll = new ScrollView(this);
        gridContainer = new LinearLayout(this);
        gridContainer.setOrientation(LinearLayout.VERTICAL);
        gridContainer.setPadding(2,2,2,2);
        scroll.addView(gridContainer);
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1f));

        LinearLayout setup = new LinearLayout(this);
        setup.setOrientation(LinearLayout.VERTICAL);
        setup.setPadding(8,4,8,6);
        setup.setBackgroundColor(Color.rgb(24,32,52));
        TextView label = new TextView(this);
        label.setText("SET LINK — screen number 1 to 16");
        label.setTextColor(Color.WHITE);
        label.setTextSize(12);
        setup.addView(label);

        LinearLayout fields = new LinearLayout(this);
        fields.setGravity(Gravity.CENTER_VERTICAL);
        numberInput = new EditText(this);
        numberInput.setSingleLine(true);
        numberInput.setHint("No. 1–16");
        numberInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        numberInput.setTextColor(Color.WHITE);
        numberInput.setHintTextColor(Color.LTGRAY);
        fields.addView(numberInput, new LinearLayout.LayoutParams(0, dp(46), 1f));

        urlInput = new EditText(this);
        urlInput.setSingleLine(true);
        urlInput.setHint("Website URL");
        urlInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);
        urlInput.setTextColor(Color.WHITE);
        urlInput.setHintTextColor(Color.LTGRAY);
        fields.addView(urlInput, new LinearLayout.LayoutParams(0, dp(46), 3f));

        Button open = new Button(this);
        open.setText("Open");
        fields.addView(open);
        setup.addView(fields);
        root.addView(setup);
        setContentView(root);

        for (int i=0; i<16; i++) {
            WebView web = new WebView(this);
            web.setBackgroundColor(Color.WHITE);
            web.getSettings().setJavaScriptEnabled(true);
            web.getSettings().setDomStorageEnabled(true);
            web.getSettings().setLoadsImagesAutomatically(true);
            web.getSettings().setSupportMultipleWindows(false);
            web.setWebViewClient(new WebViewClient());
            web.setWebChromeClient(new WebChromeClient());
            screens.add(web);
            String saved = prefs.getString("url_" + (i+1), "");
            if (!saved.isEmpty()) web.loadUrl(saved);
        }
        buildGrid();
        open.setOnClickListener(v -> openSelected());
    }

    private void openSelected() {
        String n = numberInput.getText().toString().trim();
        String url = urlInput.getText().toString().trim();
        if (n.isEmpty() || url.isEmpty()) {
            Toast.makeText(this, "Enter screen number and URL", Toast.LENGTH_SHORT).show();
            return;
        }
        int number;
        try { number = Integer.parseInt(n); } catch (Exception e) { number = -1; }
        if (number < 1 || number > 16) {
            Toast.makeText(this, "Screen number must be 1–16", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!url.matches("(?i)^https?://.*")) url = "https://" + url;
        if (!url.startsWith("https://")) {
            Toast.makeText(this, "Use HTTPS websites for security", Toast.LENGTH_LONG).show();
            return;
        }
        screens.get(number-1).loadUrl(url);
        prefs.edit().putString("url_" + number, url).apply();
        ((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE))
            .hideSoftInputFromWindow(urlInput.getWindowToken(), 0);
        numberInput.setText("");
        urlInput.setText("");
        Toast.makeText(this, String.format(Locale.US, "Screen %02d loaded and saved", number),
            Toast.LENGTH_SHORT).show();
    }

    private void buildGrid() {
        if (gridContainer == null || screens.size() != 16) return;
        gridContainer.removeAllViews();
        int rows = (int)Math.ceil(16.0 / columns);
        for (int r=0; r<rows; r++) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            for (int c=0; c<columns; c++) {
                int index = r*columns+c;
                if (index >= 16) break;
                LinearLayout tile = new LinearLayout(this);
                tile.setOrientation(LinearLayout.VERTICAL);
                tile.setPadding(1,1,1,1);
                tile.setBackgroundColor(Color.rgb(40,49,70));
                TextView serial = new TextView(this);
                serial.setText(String.format(Locale.US, "%02d", index+1));
                serial.setTextColor(Color.WHITE);
                serial.setTextSize(12);
                serial.setGravity(Gravity.CENTER);
                serial.setTypeface(null, Typeface.BOLD);
                serial.setBackgroundColor(Color.rgb(40,49,70));
                tile.addView(serial, new LinearLayout.LayoutParams(-1, dp(22)));

                WebView web = screens.get(index);
                if (web.getParent() != null) ((ViewGroup)web.getParent()).removeView(web);
                tile.addView(web, new LinearLayout.LayoutParams(-1, dp(columns == 4 ? 155 : 280)));
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, -2, 1f);
                lp.setMargins(2,2,2,2);
                row.addView(tile, lp);
            }
            gridContainer.addView(row);
        }
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    @Override public void onBackPressed() {
        for (WebView w : screens) if (w.canGoBack()) { w.goBack(); return; }
        super.onBackPressed();
    }

    @Override protected void onDestroy() {
        for (WebView w : screens) { w.stopLoading(); w.destroy(); }
        super.onDestroy();
    }
}
