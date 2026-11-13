package de.bkb.rackvault.ui;

import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import de.bkb.rackvault.R;
import de.bkb.rackvault.data.PrefsManager;

/**
 * Untermenü zur Konfiguration des globalen sowie der kategoriespezifischen
 * Formate für fortlaufende Inventarnummern (* als Zähler-Platzhalter).
 */
public class InventoryPatternActivity extends AppCompatActivity {

    private TextInputEditText etInvPatternGlobal;
    private Spinner spAddCategoryPattern;
    private TextView tvEmptyCategoryPatterns;
    private LinearLayout containerCategoryPatterns;

    private final List<String> availableCategories = new ArrayList<>();
    private final Map<String, CategoryRow> activeRows = new LinkedHashMap<>();
    private PrefsManager prefsManager;
    private boolean isUpdatingDropdown = false;

    private static final class CategoryRow {
        final View rowView;
        final TextInputEditText input;
        boolean removed = false;

        CategoryRow(View rowView, TextInputEditText input) {
            this.rowView = rowView;
            this.input = input;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory_pattern);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        prefsManager = new PrefsManager(this);

        View rootInvPattern = findViewById(R.id.rootInvPattern);
        UiUtils.setupSystemBars(this, rootInvPattern, toolbar);

        etInvPatternGlobal = findViewById(R.id.etInvPatternGlobal);
        spAddCategoryPattern = findViewById(R.id.spAddCategoryPattern);
        tvEmptyCategoryPatterns = findViewById(R.id.tvEmptyCategoryPatterns);
        containerCategoryPatterns = findViewById(R.id.containerCategoryPatterns);

        loadPatterns();
        setupDropdownListener();

        MaterialButton btnSaveInvPatterns = findViewById(R.id.btnSaveInvPatterns);
        btnSaveInvPatterns.setOnClickListener(v -> savePatterns());
    }

    private void loadPatterns() {
        etInvPatternGlobal.setText(prefsManager.getGlobalInventoryPattern());
        containerCategoryPatterns.removeAllViews();
        activeRows.clear();
        availableCategories.clear();

        for (String category : prefsManager.getCategories()) {
            String pattern = prefsManager.getCategoryInventoryPattern(category);
            if (!pattern.isEmpty()) {
                addCategoryRow(category, pattern, false);
            } else {
                availableCategories.add(category);
            }
        }
        refreshDropdown();
        refreshEmptyState();
    }

    private void setupDropdownListener() {
        spAddCategoryPattern.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (isUpdatingDropdown || position <= 0) {
                    return;
                }
                int listIdx = position - 1;
                if (listIdx >= availableCategories.size()) {
                    return;
                }
                String selectedCategory = availableCategories.remove(listIdx);
                addCategoryRow(selectedCategory, "", true);
                refreshDropdown();
                refreshEmptyState();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void addCategoryRow(String category, String initialPattern, boolean focusInput) {
        LinearLayout row = new LinearLayout(this);
        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        rowParams.topMargin = dpToPx(8);
        row.setLayoutParams(rowParams);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);

        TextInputLayout til = new TextInputLayout(this);
        LinearLayout.LayoutParams tilParams = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
        );
        tilParams.setMarginEnd(dpToPx(8));
        til.setLayoutParams(tilParams);
        til.setHint(category + " (z. B. IT-***-SWT)");

        TextInputEditText editText = new TextInputEditText(til.getContext());
        editText.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        editText.setInputType(InputType.TYPE_CLASS_TEXT
                | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
                | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        editText.setMaxLines(1);
        editText.setText(initialPattern);
        til.addView(editText);

        MaterialButton btnDelete = new MaterialButton(this, null, com.google.android.material.R.attr.materialButtonOutlinedStyle);
        btnDelete.setText(R.string.btn_delete_entry);
        btnDelete.setTextColor(getColor(R.color.status_defect_text));

        CategoryRow entry = new CategoryRow(row, editText);
        activeRows.put(category, entry);

        btnDelete.setOnClickListener(v -> {
            editText.clearFocus();
            removeCategoryRow(category, true);
        });

        editText.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus && !entry.removed && TextUtils.isEmpty(getTrimmedText(editText))) {
                containerCategoryPatterns.post(() -> {
                    if (!entry.removed && TextUtils.isEmpty(getTrimmedText(editText))) {
                        removeCategoryRow(category, true);
                    }
                });
            }
        });

        row.addView(til);
        row.addView(btnDelete);
        containerCategoryPatterns.addView(row);

        if (focusInput) {
            editText.requestFocus();
        }
    }

    private void removeCategoryRow(String category, boolean refreshUi) {
        CategoryRow entry = activeRows.remove(category);
        if (entry == null || entry.removed) {
            return;
        }
        entry.removed = true;
        containerCategoryPatterns.removeView(entry.rowView);
        prefsManager.removeCategoryInventoryPattern(category);

        if (!availableCategories.contains(category)) {
            availableCategories.add(category);
            sortAvailableCategories();
        }
        if (refreshUi) {
            refreshDropdown();
            refreshEmptyState();
        }
    }

    private void sortAvailableCategories() {
        List<String> masterOrder = prefsManager.getCategories();
        List<String> sorted = new ArrayList<>();
        for (String cat : masterOrder) {
            if (availableCategories.contains(cat)) {
                sorted.add(cat);
            }
        }
        for (String remaining : availableCategories) {
            if (!sorted.contains(remaining)) {
                sorted.add(remaining);
            }
        }
        availableCategories.clear();
        availableCategories.addAll(sorted);
    }

    private void refreshDropdown() {
        isUpdatingDropdown = true;
        List<String> items = new ArrayList<>(availableCategories.size() + 1);
        items.add(getString(R.string.prompt_add_category_pattern));
        items.addAll(availableCategories);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spAddCategoryPattern.setAdapter(adapter);
        spAddCategoryPattern.setSelection(0, false);
        spAddCategoryPattern.setEnabled(!availableCategories.isEmpty());
        isUpdatingDropdown = false;
    }

    private void refreshEmptyState() {
        tvEmptyCategoryPatterns.setVisibility(activeRows.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void savePatterns() {
        String globalPattern = getTrimmedText(etInvPatternGlobal);
        if (!PrefsManager.isValidInventoryPattern(globalPattern)) {
            etInvPatternGlobal.setError("Mindestens ein * als Zähler erforderlich (z. B. IT-***)");
            etInvPatternGlobal.requestFocus();
            return;
        }
        etInvPatternGlobal.setError(null);

        Map<String, String> categoryPatterns = new LinkedHashMap<>();
        List<String> emptyCategoriesToRemove = new ArrayList<>();

        for (Map.Entry<String, CategoryRow> mapEntry : activeRows.entrySet()) {
            String category = mapEntry.getKey();
            TextInputEditText field = mapEntry.getValue().input;
            String value = getTrimmedText(field);
            if (TextUtils.isEmpty(value)) {
                emptyCategoriesToRemove.add(category);
                continue;
            }
            if (!PrefsManager.isValidInventoryPattern(value)) {
                field.setError("Muster muss mindestens ein * enthalten (z. B. IT-***-SWT)");
                field.requestFocus();
                return;
            }
            field.setError(null);
            categoryPatterns.put(category, value);
        }

        for (String emptyCategory : emptyCategoriesToRemove) {
            removeCategoryRow(emptyCategory, false);
            categoryPatterns.put(emptyCategory, "");
        }
        refreshDropdown();
        refreshEmptyState();

        prefsManager.saveInventoryPatterns(globalPattern, categoryPatterns);
        Toast.makeText(this, "Inventarnummer-Formatierung gespeichert", Toast.LENGTH_SHORT).show();
        finish();
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }

    private static String getTrimmedText(TextInputEditText editText) {
        return editText.getText() != null ? editText.getText().toString().trim() : "";
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
