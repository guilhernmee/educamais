package br.com.educamais.app;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Insets;
import android.os.Build;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;

/**
 * O Android 15 desenha o app por baixo da barra de status e da barra de navegação.
 * Aqui o conteúdo ganha um recuo do tamanho dessas barras (e do teclado, quando aberto).
 */
final class Bordas {

    private Bordas() {
    }

    @SuppressWarnings("deprecation")
    static void ajustar(Activity activity, View raiz, boolean iconesEscuros) {
        Window janela = activity.getWindow();
        janela.setStatusBarColor(Color.TRANSPARENT);
        janela.setNavigationBarColor(Color.TRANSPARENT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            janela.setDecorFitsSystemWindows(false);
            WindowInsetsController controle = janela.getInsetsController();
            if (controle != null) {
                int claros = WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS | WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS;
                controle.setSystemBarsAppearance(iconesEscuros ? claros : 0, claros);
            }
        } else {
            int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
            if (iconesEscuros) {
                flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
            }
            janela.getDecorView().setSystemUiVisibility(flags);
        }
        raiz.setOnApplyWindowInsetsListener((v, insets) -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                Insets barras = insets.getInsets(WindowInsets.Type.systemBars() | WindowInsets.Type.displayCutout());
                Insets teclado = insets.getInsets(WindowInsets.Type.ime());
                v.setPadding(barras.left, barras.top, barras.right, Math.max(barras.bottom, teclado.bottom));
                return WindowInsets.CONSUMED;
            }
            v.setPadding(insets.getSystemWindowInsetLeft(), insets.getSystemWindowInsetTop(),
                    insets.getSystemWindowInsetRight(), insets.getSystemWindowInsetBottom());
            return insets.consumeSystemWindowInsets();
        });
        raiz.requestApplyInsets();
    }
}
