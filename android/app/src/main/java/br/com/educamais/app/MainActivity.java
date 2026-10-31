package br.com.educamais.app;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.webkit.ConsoleMessage;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import org.json.JSONObject;

/**
 * Mostra as telas do Educa+ (assets/www/index.html) e liga essas telas à API pela ponte
 * "EducaAndroid": o JavaScript pede, o Java faz a chamada HTTP e devolve a resposta.
 */
public class MainActivity extends Activity {

    private static final String TELAS = "file:///android_asset/www/index.html";
    private WebView web;

    @SuppressLint({"SetJavaScriptEnabled", "AddJavascriptInterface"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Preferencias.usuario(this) == null) {
            voltarAoLogin();
            return;
        }
        setContentView(R.layout.activity_main);
        Bordas.ajustar(this, findViewById(R.id.raiz), true);

        web = findViewById(R.id.web);
        WebSettings ajustes = web.getSettings();
        ajustes.setJavaScriptEnabled(true);
        ajustes.setDomStorageEnabled(true);
        ajustes.setAllowFileAccess(true);
        ajustes.setTextZoom(100);
        web.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onConsoleMessage(ConsoleMessage m) {
                Log.d("EducaTelas", m.message() + " (linha " + m.lineNumber() + ")");
                return true;
            }
        });
        web.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri uri = request.getUrl();
                if ("file".equals(uri.getScheme())) {
                    return false;
                }
                startActivity(new Intent(Intent.ACTION_VIEW, uri));
                return true;
            }
        });
        web.addJavascriptInterface(new Ponte(), "EducaAndroid");
        web.loadUrl(TELAS);
    }

    /** Voltar do Android: as telas decidem (fecha folha, sai do nível, volta uma tela). */
    @SuppressWarnings("deprecation")
    @Override
    public void onBackPressed() {
        if (web == null) {
            super.onBackPressed();
            return;
        }
        web.evaluateJavascript("window.educaVoltar ? window.educaVoltar() : false", resultado -> {
            if (!"true".equals(resultado)) {
                moveTaskToBack(true);
            }
        });
    }

    @Override
    protected void onDestroy() {
        if (web != null) {
            web.removeJavascriptInterface("EducaAndroid");
            web.destroy();
            web = null;
        }
        super.onDestroy();
    }

    private void voltarAoLogin() {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    /** Métodos que o JavaScript das telas enxerga como window.EducaAndroid. */
    private class Ponte {

        @JavascriptInterface
        public String usuarioId() {
            return Preferencias.usuario(MainActivity.this);
        }

        @JavascriptInterface
        public void request(String id, String metodo, String caminho, String corpo) {
            ApiClient.chamar(Preferencias.servidor(MainActivity.this), metodo, caminho, corpo, (status, texto) -> {
                if (web != null) {
                    web.evaluateJavascript("window.__educaApi(" + JSONObject.quote(id) + "," + status + ","
                            + JSONObject.quote(texto) + ")", null);
                }
            });
        }

        @JavascriptInterface
        public void sair() {
            runOnUiThread(() -> {
                Preferencias.sair(MainActivity.this);
                voltarAoLogin();
            });
        }
    }
}
