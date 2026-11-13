package de.bkb.rackvault.ui;

import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.List;

import de.bkb.rackvault.R;
import de.bkb.rackvault.data.DatabaseHelper;
import de.bkb.rackvault.data.PrefsManager;

/**
 * Untermenü zur Verwaltung (Anlegen, Bearbeiten, Löschen) eigener Standorte und Kategorien.
 */
public class ManageListActivity extends AppCompatActivity {

    public static final String EXTRA_LIST_TYPE = "extra_list_type";
    public static final String TYPE_LOCATIONS = "locations";
    public static final String TYPE_CATEGORIES = "categories";

    private PrefsManager prefsManager;
    private DatabaseHelper dbHelper;
    private String listType;
    private TextInputEditText etNewEntry;
    private LinearLayout containerEntries;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_list);

        String requestedType = getIntent().getStringExtra(EXTRA_LIST_TYPE);
        listType = TYPE_CATEGORIES.equals(requestedType) ? TYPE_CATEGORIES : TYPE_LOCATIONS;

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(isCategoriesMode()
                    ? R.string.title_manage_categories
                    : R.string.title_manage_locations);
        }

        View rootManageList = findViewById(R.id.rootManageList);
        UiUtils.setupSystemBars(this, rootManageList, toolbar);

        prefsManager = new PrefsManager(this);
        dbHelper = new DatabaseHelper(this);

        TextInputLayout tilNewEntry = findViewById(R.id.tilNewEntry);
        tilNewEntry.setHint(getString(isCategoriesMode()
                ? R.string.hint_new_category
                : R.string.hint_new_location));

        etNewEntry = findViewById(R.id.etNewEntry);
        containerEntries = findViewById(R.id.containerEntries);
        MaterialButton btnAddEntry = findViewById(R.id.btnAddEntry);

        btnAddEntry.setOnClickListener(v -> addNewEntry());
        renderEntries();
    }

    private boolean isCategoriesMode() {
        return TYPE_CATEGORIES.equals(listType);
    }

    private List<String> loadItems() {
        return isCategoriesMode() ? prefsManager.getCategories() : prefsManager.getLocations();
    }

    private void saveItems(List<String> items) {
        if (isCategoriesMode()) {
            prefsManager.saveCategories(items);
        } else {
            prefsManager.saveLocations(items);
        }
    }

    private void addNewEntry() {
        String value = etNewEntry.getText() != null ? etNewEntry.getText().toString().trim() : "";
        if (TextUtils.isEmpty(value)) {
            etNewEntry.setError("Bitte einen Namen eingeben");
            etNewEntry.requestFocus();
            return;
        }
        List<String> items = loadItems();
        for (String existing : items) {
            if (existing.equalsIgnoreCase(value)) {
                etNewEntry.setError("Eintrag existiert bereits");
                etNewEntry.requestFocus();
                return;
            }
        }
        etNewEntry.setError(null);
        items.add(value);
        saveItems(items);
        etNewEntry.setText("");
        renderEntries();
        Toast.makeText(this, "Hinzugefügt: " + value, Toast.LENGTH_SHORT).show();
    }

    private void renderEntries() {
        containerEntries.removeAllViews();
        List<String> items = loadItems();
        int marginBottom = dpToPx(8);
        int cardPadding = dpToPx(12);

        for (String item : items) {
            MaterialCardView card = new MaterialCardView(this);
            LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            cardParams.bottomMargin = marginBottom;
            card.setLayoutParams(cardParams);
            card.setRadius(dpToPx(8));
            card.setCardElevation(0f);
            card.setStrokeWidth(dpToPx(1));
            card.setStrokeColor(getColor(R.color.surface_border));

            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(cardPadding, cardPadding, cardPadding, cardPadding);

            TextView tvLabel = new TextView(this);
            LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
            );
            tvLabel.setLayoutParams(labelParams);
            tvLabel.setText(item);
            tvLabel.setTextSize(15f);

            MaterialButton btnEdit = new MaterialButton(this, null, com.google.android.material.R.attr.materialButtonOutlinedStyle);
            LinearLayout.LayoutParams editParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            editParams.setMarginEnd(dpToPx(6));
            btnEdit.setLayoutParams(editParams);
            btnEdit.setText(R.string.btn_edit_entry);
            btnEdit.setOnClickListener(v -> showRenameDialog(item));

            MaterialButton btnDelete = new MaterialButton(this, null, com.google.android.material.R.attr.materialButtonOutlinedStyle);
            btnDelete.setText(R.string.btn_delete_entry);
            btnDelete.setTextColor(getColor(R.color.status_defect_text));
            btnDelete.setOnClickListener(v -> deleteEntry(item));

            row.addView(tvLabel);
            row.addView(btnEdit);
            row.addView(btnDelete);
            card.addView(row);
            containerEntries.addView(card);
        }
    }

    private void showRenameDialog(String oldName) {
        EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        input.setText(oldName);
        input.setSelection(oldName.length());

        int pad = dpToPx(20);
        LinearLayout wrapper = new LinearLayout(this);
        wrapper.setPadding(pad, dpToPx(8), pad, 0);
        wrapper.addView(input, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        new AlertDialog.Builder(this)
                .setTitle(isCategoriesMode() ? "Kategorie bearbeiten" : "Standort bearbeiten")
                .setView(wrapper)
                .setPositiveButton("Speichern", (dialog, which) -> {
                    String newName = input.getText() != null ? input.getText().toString().trim() : "";
                    if (TextUtils.isEmpty(newName)) {
                        Toast.makeText(this, "Name darf nicht leer sein", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (newName.equals(oldName)) {
                        return;
                    }
                    List<String> items = loadItems();
                    for (String existing : items) {
                        if (!existing.equals(oldName) && existing.equalsIgnoreCase(newName)) {
                            Toast.makeText(this, "Eintrag existiert bereits", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                    if (isCategoriesMode()) {
                        prefsManager.renameCategory(oldName, newName);
                        dbHelper.renameCategoryInAssets(oldName, newName);
                    } else {
                        prefsManager.renameLocation(oldName, newName);
                        dbHelper.renameLocationInAssets(oldName, newName);
                    }
                    renderEntries();
                    Toast.makeText(this, "Umbenannt in: " + newName, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Abbrechen", null)
                .show();
    }

    private void deleteEntry(String entry) {
        List<String> items = loadItems();
        if (items.size() <= 1) {
            Toast.makeText(this, "Mindestens ein Eintrag muss bestehen bleiben", Toast.LENGTH_SHORT).show();
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle(isCategoriesMode() ? "Kategorie löschen" : "Standort löschen")
                .setMessage("Soll \"" + entry + "\" aus der Auswahlliste entfernt werden?")
                .setPositiveButton("Löschen", (dialog, which) -> {
                    items.remove(entry);
                    saveItems(items);
                    if (isCategoriesMode()) {
                        prefsManager.removeCategoryInventoryPattern(entry);
                    } else if (entry.equals(prefsManager.getDefaultLocation()) && !items.isEmpty()) {
                        prefsManager.saveSettings(prefsManager.getAdminCode(), items.get(0), prefsManager.isWifiOnlyLookup());
                    }
                    renderEntries();
                    Toast.makeText(this, "Entfernt: " + entry, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Abbrechen", null)
                .show();
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            UiUtils.hideNavigationBar(this);
        }
    }
}
