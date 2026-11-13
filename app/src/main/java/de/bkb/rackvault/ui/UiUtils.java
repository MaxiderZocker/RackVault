package de.bkb.rackvault.ui;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

/**
 * Hilfsklasse für Edge-to-Edge-Insets (Statusleiste/Cutout) und das Ausblenden der System-Navigationsleiste.
 */
public final class UiUtils {

    private UiUtils() {
    }

    /**
     * Blendet die untere System-Navigationsleiste im transienten Modus aus und passt Toolbar- sowie
     * Root-Abstände an Statusleiste und Display-Cutout an.
     */
    public static void setupSystemBars(@NonNull AppCompatActivity activity,
                                       @NonNull View rootView,
                                       @NonNull View toolbar) {
        hideNavigationBar(activity);

        ViewGroup.LayoutParams initialParams = toolbar.getLayoutParams();
        final int baseToolbarHeight = initialParams != null ? initialParams.height : ViewGroup.LayoutParams.WRAP_CONTENT;

        ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, windowInsets) -> {
            Insets bars = windowInsets.getInsets(
                    WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());

            toolbar.setPadding(0, bars.top, 0, 0);
            if (baseToolbarHeight > 0) {
                ViewGroup.LayoutParams lp = toolbar.getLayoutParams();
                if (lp != null && lp.height != baseToolbarHeight + bars.top) {
                    lp.height = baseToolbarHeight + bars.top;
                    toolbar.setLayoutParams(lp);
                }
            }

            v.setPadding(bars.left, 0, bars.right, bars.bottom);
            return windowInsets;
        });

        ViewCompat.requestApplyInsets(rootView);
    }

    /**
     * Blendet die System-Navigationsleiste aus; Wischen vom Rand blendet sie vorübergehend wieder ein.
     */
    public static void hideNavigationBar(@NonNull AppCompatActivity activity) {
        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(
                activity.getWindow(), activity.getWindow().getDecorView());
        if (controller != null) {
            controller.setSystemBarsBehavior(
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            controller.hide(WindowInsetsCompat.Type.navigationBars());
        }
    }
}
