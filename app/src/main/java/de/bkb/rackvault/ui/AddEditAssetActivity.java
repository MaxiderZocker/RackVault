package de.bkb.rackvault.ui;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;

import de.bkb.rackvault.R;

/**
 * Formular-Activity zum Anlegen neuer sowie Bearbeiten vorhandener IT-Geräte.
 */
public class AddEditAssetActivity extends AppCompatActivity {

    public static final String EXTRA_ASSET_ID = "extra_asset_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_asset);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }
}
