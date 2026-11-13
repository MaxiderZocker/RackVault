package de.bkb.rackvault.ui;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;

import de.bkb.rackvault.R;

/**
 * Detailansicht für ein Gerät inkl. Ausleihe, Rückgabe und Ausleih-Verlauf.
 */
public class AssetDetailActivity extends AppCompatActivity {

    public static final String EXTRA_ASSET_ID = "extra_asset_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asset_detail);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }
}
