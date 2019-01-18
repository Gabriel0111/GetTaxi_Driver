package gabriel.gettaxi_driver.controller;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebView;

import gabriel.gettaxi_driver.R;

public class GetTaxiWeb extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_taxi_web);

        WebView webView = (WebView) findViewById(R.id.get_taxi_web_webview);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl("https://gett.com/il/about/");

    }
}
