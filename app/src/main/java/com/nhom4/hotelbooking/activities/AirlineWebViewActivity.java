package com.nhom4.hotelbooking.activities;

import android.os.Bundle;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.nhom4.hotelbooking.R;

public class AirlineWebViewActivity extends AppCompatActivity {

    private WebView webView;
    private ProgressBar progressBar;
    private TextView tvWebTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview_airline);

        webView = findViewById(R.id.webViewAirline);
        progressBar = findViewById(R.id.progressWeb);
        tvWebTitle = findViewById(R.id.tvWebTitle);

        String url = getIntent().getStringExtra("url");
        String name = getIntent().getStringExtra("name");

        if (name != null) {
            tvWebTitle.setText(name);
        }

        findViewById(R.id.btnBackWeb).setOnClickListener(v -> finish());

        setupWebView(url);
    }

    private void setupWebView(String url) {
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, android.graphics.Bitmap favicon) {
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(AirlineWebViewActivity.this, "Lỗi tải trang", Toast.LENGTH_SHORT).show();
            }
        });

        if (url != null) {
            webView.loadUrl(url);
        }
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}