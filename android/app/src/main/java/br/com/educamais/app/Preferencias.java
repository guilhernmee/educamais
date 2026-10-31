package br.com.educamais.app;

import android.content.Context;
import android.content.SharedPreferences;

/** Guarda no celular o endereço da API e quem está logado. */
public final class Preferencias {

    /** 10.0.2.2 é o "localhost" do computador visto de dentro do emulador do Android Studio. */
    public static final String SERVIDOR_PADRAO = "http://10.0.2.2:8080";

    private Preferencias() {
    }

    private static SharedPreferences prefs(Context c) {
        return c.getSharedPreferences("educa", Context.MODE_PRIVATE);
    }

    public static String servidor(Context c) {
        return prefs(c).getString("servidor", SERVIDOR_PADRAO);
    }

    public static void salvarServidor(Context c, String url) {
        prefs(c).edit().putString("servidor", url).apply();
    }

    public static String usuario(Context c) {
        return prefs(c).getString("usuario", null);
    }

    public static void salvarUsuario(Context c, String id) {
        prefs(c).edit().putString("usuario", id).apply();
    }

    public static void sair(Context c) {
        prefs(c).edit().remove("usuario").apply();
    }
}
