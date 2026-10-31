package br.com.educamais.app;

import android.os.Handler;
import android.os.Looper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.json.JSONObject;

/**
 * Faz as chamadas HTTP para a API do Educa+ fora da thread principal e devolve
 * o resultado na thread principal. Usado pela tela de login e pela ponte das telas do jogo.
 */
public final class ApiClient {

    /** Resultado de uma chamada: status HTTP (0 = sem conexão) e o corpo da resposta em JSON. */
    public interface Resposta {
        void chegou(int status, String corpo);
    }

    private static final ExecutorService FILA = Executors.newFixedThreadPool(3);
    private static final Handler PRINCIPAL = new Handler(Looper.getMainLooper());

    private ApiClient() {
    }

    public static void chamar(String servidor, String metodo, String caminho, String corpo, Resposta resposta) {
        FILA.execute(() -> {
            int status;
            String texto;
            HttpURLConnection conexao = null;
            try {
                if (!caminho.startsWith("/api/")) {
                    throw new IOException("Caminho não permitido: " + caminho);
                }
                URL url = new URL(servidor.replaceAll("/+$", "") + caminho);
                conexao = (HttpURLConnection) url.openConnection();
                conexao.setConnectTimeout(8000);
                conexao.setReadTimeout(20000);
                conexao.setRequestMethod(metodo);
                conexao.setRequestProperty("Accept", "application/json");
                if (corpo != null && !corpo.isEmpty()) {
                    conexao.setDoOutput(true);
                    conexao.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                    try (OutputStream saida = conexao.getOutputStream()) {
                        saida.write(corpo.getBytes(StandardCharsets.UTF_8));
                    }
                }
                status = conexao.getResponseCode();
                InputStream entrada = status >= 400 ? conexao.getErrorStream() : conexao.getInputStream();
                texto = entrada == null ? "" : ler(entrada);
            } catch (IOException e) {
                status = 0;
                texto = "{\"erro\":" + JSONObject.quote("Sem conexão com " + servidor
                        + ". Confira se a API está rodando e se o celular está na mesma Wi-Fi do computador.") + "}";
            } finally {
                if (conexao != null) {
                    conexao.disconnect();
                }
            }
            final int s = status;
            final String t = texto;
            PRINCIPAL.post(() -> resposta.chegou(s, t));
        });
    }

    private static String ler(InputStream entrada) throws IOException {
        try (InputStream in = entrada; ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
            byte[] bloco = new byte[8192];
            int n;
            while ((n = in.read(bloco)) != -1) {
                buffer.write(bloco, 0, n);
            }
            return buffer.toString(StandardCharsets.UTF_8.name());
        }
    }

    /** Tira a mensagem do {"erro": "..."} que a API devolve. */
    public static String mensagemDeErro(String corpo) {
        try {
            return new JSONObject(corpo).optString("erro", "Erro inesperado no servidor.");
        } catch (Exception e) {
            return "Erro inesperado no servidor.";
        }
    }
}
