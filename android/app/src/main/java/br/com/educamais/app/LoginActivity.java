package br.com.educamais.app;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Entrada do app. Por enquanto lista as contas de teste do banco; quando o login Google
 * estiver configurado, o botão chama o Google e manda o e-mail confirmado para /api/auth/login.
 */
public class LoginActivity extends Activity {

    private LinearLayout contas;
    private TextView status;
    private EditText servidor;
    private Button entrar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Preferencias.usuario(this) != null) {
            abrirApp();
            return;
        }
        setContentView(R.layout.activity_login);
        Bordas.ajustar(this, findViewById(R.id.raiz), false);

        TextView logo = findViewById(R.id.logo);
        SpannableString texto = new SpannableString("Educa+");
        texto.setSpan(new ForegroundColorSpan(getColor(R.color.estrela)), 5, 6, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        logo.setText(texto);

        contas = findViewById(R.id.contas);
        status = findViewById(R.id.status);
        servidor = findViewById(R.id.servidor);
        entrar = findViewById(R.id.entrar);
        servidor.setText(Preferencias.servidor(this));

        entrar.setOnClickListener(v -> buscarContas());
        findViewById(R.id.salvarServidor).setOnClickListener(v -> buscarContas());
        findViewById(R.id.mostrarServidor).setOnClickListener(v -> {
            View caixa = findViewById(R.id.caixaServidor);
            caixa.setVisibility(caixa.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
        });
    }

    private void salvarServidor() {
        String url = servidor.getText().toString().trim();
        if (url.isEmpty()) {
            url = Preferencias.SERVIDOR_PADRAO;
        }
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "http://" + url;
        }
        Preferencias.salvarServidor(this, url);
        servidor.setText(url);
    }

    private void buscarContas() {
        salvarServidor();
        status.setText(R.string.buscando_contas);
        entrar.setEnabled(false);
        ApiClient.chamar(Preferencias.servidor(this), "GET", "/api/auth/contas-teste", null, (codigo, corpo) -> {
            entrar.setEnabled(true);
            if (codigo != 200) {
                status.setText(ApiClient.mensagemDeErro(corpo));
                findViewById(R.id.caixaServidor).setVisibility(View.VISIBLE);
                return;
            }
            try {
                mostrarContas(new JSONArray(corpo));
            } catch (JSONException e) {
                status.setText(R.string.resposta_invalida);
            }
        });
    }

    private void mostrarContas(JSONArray lista) throws JSONException {
        contas.removeAllViews();
        for (int i = 0; i < lista.length(); i++) {
            JSONObject conta = lista.getJSONObject(i);
            String nome = conta.getString("nome");
            String email = conta.getString("email");
            String perfil = conta.getString("perfil");

            View item = getLayoutInflater().inflate(R.layout.item_conta, contas, false);
            TextView iniciais = item.findViewById(R.id.iniciais);
            iniciais.setText(iniciais(nome));
            Drawable fundo = iniciais.getBackground().mutate();
            fundo.setTint(getColor(perfil.equals("ALUNO") ? R.color.mat : perfil.equals("PROFESSOR") ? R.color.azul : R.color.verde));
            iniciais.setBackground(fundo);
            ((TextView) item.findViewById(R.id.nome)).setText(nome);
            ((TextView) item.findViewById(R.id.email)).setText(email);
            ((TextView) item.findViewById(R.id.detalhe)).setText(conta.optString("detalhe", perfil));
            item.setOnClickListener(v -> entrarComo(email));
            contas.addView(item);
        }
        contas.setVisibility(View.VISIBLE);
        entrar.setVisibility(View.GONE);
        status.setText(R.string.escolha_conta);
    }

    private void entrarComo(String email) {
        status.setText(R.string.entrando);
        String corpo;
        try {
            corpo = new JSONObject().put("email", email).toString();
        } catch (JSONException e) {
            return;
        }
        ApiClient.chamar(Preferencias.servidor(this), "POST", "/api/auth/login", corpo, (codigo, resposta) -> {
            if (codigo != 200) {
                status.setText(ApiClient.mensagemDeErro(resposta));
                return;
            }
            try {
                Preferencias.salvarUsuario(this, new JSONObject(resposta).getString("id"));
                abrirApp();
            } catch (JSONException e) {
                status.setText(R.string.resposta_invalida);
            }
        });
    }

    private void abrirApp() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private static String iniciais(String nome) {
        StringBuilder sb = new StringBuilder();
        for (String parte : nome.trim().split("\\s+")) {
            if (!parte.isEmpty() && sb.length() < 2) {
                sb.append(Character.toUpperCase(parte.charAt(0)));
            }
        }
        return sb.toString();
    }
}
