package fantaq.fanta;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import fantaq.fanta.ui.AlarmLayout;
import fantaq.fanta.ui.Theme;
import trikita.anvil.Anvil;
import trikita.anvil.RenderableView;

public class MainActivity extends Activity {

    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);
        updateTheme();
        setContentView(new RenderableView(this) {
            public void view() {
                AlarmLayout.view();
            }
        });
    }

    public void onResume() {
        super.onResume();
        updateTheme();
        Anvil.render();
    }

    public void openSettings() {
        startActivity(new Intent(this, SettingsActivity.class));
    }

    private void updateTheme() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            if (Theme.get(App.getState().settings().theme()).light) {
                setTheme(android.R.style.Theme_Holo_Light_NoActionBar);
            } else {
                setTheme(android.R.style.Theme_Holo_NoActionBar);
            }
        } else {
            if (Theme.get(App.getState().settings().theme()).light) {
                setTheme(android.R.style.Theme_Material_Light_NoActionBar);
            } else {
                setTheme(android.R.style.Theme_Material_NoActionBar);
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(Theme.get(App.getState().settings().theme()).primaryDarkColor);
        }
    }
}
