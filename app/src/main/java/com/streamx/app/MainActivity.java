package com.streamx.app;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import android.text.InputType;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebChromeClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends Activity {

    private static final String DEFAULT_URL =
        "https://get.atechspace.live/dl/54ce0f3a-9bd7-4b9e-b838-eac2dab0480c";

    private static final String AD_UNIT =
        "ca-app-pub-7349899618799245/8612064630";

    private final ArrayList<WebView> views = new ArrayList<>();

    private SharedPreferences prefs;
    private LinearLayout root;
    private LinearLayout grid;
    private EditText numberInput;
    private EditText urlInput;
    private TextView status;
    private RewardedAd rewardedAd;

    private final Handler handler =
        new Handler(Looper.getMainLooper());

    private View promo;

    private final Runnable promoTask = new Runnable() {
        @Override
        public void run() {
            showPromo();
            handler.postDelayed(this, 300000L);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setStatusBarColor(Color.rgb(13, 19, 35));
        getWindow().setNavigationBarColor(Color.rgb(13, 19, 35));

        prefs = getSharedPreferences(
            "streamx_links", MODE_PRIVATE
        );

        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.rgb(13, 19, 35));

        LinearLayout top = new LinearLayout(this);
        top.setGravity(Gravity.CENTER_VERTICAL);
        top.setPadding(dp(8), dp(4), dp(8), dp(4));
        top.setBackgroundColor(Color.rgb(22, 31, 52));

        TextView title = new TextView(this);
        title.setText("StreamX App");
        title.setTextColor(Color.WHITE);
        title.setTextSize(19);
        title.setTypeface(null, 1);

        top.addView(title, new LinearLayout.LayoutParams(
            0, -2, 1
        ));

        Button watchButton = new Button(this);
        watchButton.setText("Reward Ad");
        watchButton.setTextSize(10);
        top.addView(watchButton);

        watchButton.setOnClickListener(v -> showAd());

        Button reloadButton = new Button(this);
        reloadButton.setText("Reload");
        reloadButton.setTextSize(10);
        top.addView(reloadButton);

        reloadButton.setOnClickListener(v -> {
            for (WebView webView : views) {
                webView.reload();
            }
        });

        root.addView(top);

        status = new TextView(this);
        status.setText("Rewarded ad is optional.");
        status.setTextColor(Color.LTGRAY);
        status.setTextSize(11);
        status.setPadding(dp(8), dp(4), dp(8), dp(4));
        root.addView(status);

        ScrollView scroll = new ScrollView(this);
        grid = new LinearLayout(this);
        grid.setOrientation(LinearLayout.VERTICAL);
        grid.setPadding(2, 2, 2, 2);

        scroll.addView(grid);

        root.addView(scroll, new LinearLayout.LayoutParams(
            -1, 0, 1
        ));

        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(dp(7), dp(4), dp(7), dp(5));
        form.setBackgroundColor(Color.rgb(22, 31, 52));

        TextView label = new TextView(this);
        label.setText("SET LINK FOR SCREEN 01-16");
        label.setTextColor(Color.WHITE);
        label.setTextSize(12);
        form.addView(label);

        LinearLayout fields = new LinearLayout(this);
        fields.setGravity(Gravity.CENTER_VERTICAL);

        numberInput = new EditText(this);
        numberInput.setSingleLine(true);
        numberInput.setHint("No. 1-16");
        numberInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        styleInput(numberInput);

        fields.addView(numberInput,
            new LinearLayout.LayoutParams(0, dp(44), 1));

        urlInput = new EditText(this);
        urlInput.setSingleLine(true);
        urlInput.setHint("https://website...");
        urlInput.setInputType(
            InputType.TYPE_CLASS_TEXT |
            InputType.TYPE_TEXT_VARIATION_URI
        );
        styleInput(urlInput);

        fields.addView(urlInput,
            new LinearLayout.LayoutParams(0, dp(44), 3));

        Button saveButton = new Button(this);
        saveButton.setText("Save");
        fields.addView(saveButton);

        form.addView(fields);
        root.addView(form);

        setContentView(root);

        for (int i = 1; i <= 16; i++) {
            WebView webView = new WebView(this);

            webView.setBackgroundColor(Color.WHITE);

            webView.getSettings().setJavaScriptEnabled(true);
            webView.getSettings().setDomStorageEnabled(true);
            webView.getSettings().setLoadsImagesAutomatically(true);

            webView.setWebViewClient(new WebViewClient());
            webView.setWebChromeClient(new WebChromeClient());

            views.add(webView);

            String savedUrl = prefs.getString(
                "u" + i, DEFAULT_URL
            );

            webView.loadUrl(savedUrl);
        }

        buildGrid();

        saveButton.setOnClickListener(v -> saveLink());

        MobileAds.initialize(this, initializationStatus -> {
            loadAd();
        });

        handler.postDelayed(promoTask, 300000L);
    }

    private void styleInput(EditText input) {
        input.setTextColor(Color.WHITE);
        input.setHintTextColor(Color.LTGRAY);
        input.setTextSize(12);
        input.setPadding(dp(6), 0, dp(6), 0);
    }

    private void loadAd() {
        AdRequest request = new AdRequest.Builder().build();

        RewardedAd.load(
            this,
            AD_UNIT,
            request,
            new RewardedAdLoadCallback() {

                @Override
                public void onAdLoaded(RewardedAd ad) {
                    rewardedAd = ad;

                    if (status != null) {
                        status.setText(
                            "Rewarded ad ready. Watching is optional."
                        );
                    }
                }

                @Override
                public void onAdFailedToLoad(LoadAdError error) {
                    rewardedAd = null;

                    if (status != null) {
                        status.setText(
                            "Ad unavailable. You can use the app."
                        );
                    }
                }
            }
        );
    }

    private void showAd() {
        if (rewardedAd == null) {
            Toast.makeText(
                this,
                "Ad not ready. Try again later.",
                Toast.LENGTH_SHORT
            ).show();

            loadAd();
            return;
        }

        RewardedAd ad = rewardedAd;
        rewardedAd = null;

        ad.show(this, rewardItem -> {
            runOnUiThread(() ->
                Toast.makeText(
                    MainActivity.this,
                    "Thanks for watching!",
                    Toast.LENGTH_SHORT
                ).show()
            );
        });

        loadAd();
    }

    private void saveLink() {
        String number = numberInput.getText()
            .toString().trim();

        String url = urlInput.getText()
            .toString().trim();

        if (number.isEmpty() || url.isEmpty()) {
            Toast.makeText(
                this,
                "Enter screen number and URL",
                Toast.LENGTH_SHORT
            ).show();
            return;
        }

        int index;

        try {
            index = Integer.parseInt(number);
        } catch (NumberFormatException e) {
            index = -1;
        }

        if (index < 1 || index > 16) {
            Toast.makeText(
                this,
                "Screen number must be 1-16",
                Toast.LENGTH_SHORT
            ).show();
            return;
        }

        if (!url.matches("(?i)^https?://.+")) {
            url = "https://" + url;
        }

        if (!url.matches("(?i)^https://.+")) {
            Toast.makeText(
                this,
                "Please enter a valid HTTPS URL",
                Toast.LENGTH_SHORT
            ).show();
            return;
        }

        prefs.edit().putString("u" + index, url).apply();

        views.get(index - 1).loadUrl(url);

        InputMethodManager imm =
            (InputMethodManager) getSystemService(
                Context.INPUT_METHOD_SERVICE
            );

        if (imm != null) {
            imm.hideSoftInputFromWindow(
                urlInput.getWindowToken(), 0
            );
        }

        numberInput.setText("");
        urlInput.setText("");

        Toast.makeText(
            this,
            "Screen " + index + " saved",
            Toast.LENGTH_SHORT
        ).show();
    }

    private void buildGrid() {
        grid.removeAllViews();

        for (int r = 0; r < 4; r++) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);

            for (int c = 0; c < 4; c++) {
                int index = r * 4 + c;

                LinearLayout tile = new LinearLayout(this);
                tile.setOrientation(LinearLayout.VERTICAL);
                tile.setBackgroundColor(Color.rgb(42, 52, 74));

                TextView serial = new TextView(this);
                serial.setText(String.format(
                    Locale.US, "%02d", index + 1
                ));
                serial.setTextColor(Color.WHITE);
                serial.setGravity(Gravity.CENTER);
                serial.setTextSize(11);
                serial.setTypeface(null, 1);

                tile.addView(serial,
                    new LinearLayout.LayoutParams(-1, dp(22)));

                WebView webView = views.get(index);

                if (webView.getParent() != null) {
                    ((ViewGroup) webView.getParent())
                        .removeView(webView);
                }

                tile.addView(webView,
                    new LinearLayout.LayoutParams(-1, dp(170)));

                LinearLayout.LayoutParams tileParams =
                    new LinearLayout.LayoutParams(0, -2, 1);

                tileParams.setMargins(2, 2, 2, 2);
                row.addView(tile, tileParams);
            }

            grid.addView(row);
        }
    }

    private void showPromo() {
        if (isFinishing() || promo != null) {
            return;
        }

        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setGravity(Gravity.CENTER);
        panel.setPadding(dp(16), dp(16), dp(16), dp(16));
        panel.setBackgroundColor(0xEE101828);

        TextView heading = new TextView(this);
        heading.setText("Sponsored content");
        heading.setTextColor(Color.WHITE);
        heading.setTextSize(20);
        heading.setGravity(Gravity.CENTER);
        panel.addView(heading);

        TextView message = new TextView(this);
        message.setText(
            "This panel closes automatically in 15 seconds."
        );
        message.setTextColor(Color.LTGRAY);
        message.setGravity(Gravity.CENTER);
        panel.addView(message);

        Button closeButton = new Button(this);
        closeButton.setText("Close");
        panel.addView(closeButton);

        closeButton.setOnClickListener(v -> removePromo(panel));

        root.addView(panel, new LinearLayout.LayoutParams(
            -1, -1
        ));

        promo = panel;

        handler.postDelayed(
            () -> removePromo(panel), 15000L
        );
    }

    private void removePromo(View view) {
        if (promo == view) {
            if (view.getParent() instanceof ViewGroup) {
                ((ViewGroup) view.getParent()).removeView(view);
            }

            promo = null;
        }
    }

    private int dp(int value) {
        return Math.round(
            value * getResources().getDisplayMetrics().density
        );
    }

    @Override
    public void onBackPressed() {
        for (WebView webView : views) {
            if (webView.canGoBack()) {
                webView.goBack();
                return;
            }
        }

        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacks(promoTask);

        for (WebView webView : views) {
            webView.stopLoading();
            webView.destroy();
        }

        super.onDestroy();
    }
          }
