package de.bkb.rackvault.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

import de.bkb.rackvault.R;
import de.bkb.rackvault.data.PrefsManager;
import de.bkb.rackvault.model.Asset;
import de.bkb.rackvault.model.LoanLog;

/**
 * Detailansicht für ein Gerät inkl. Ausleihe per AlertDialog, Rückgabe und Ausleih-Verlauf.
 */
public class AssetDetailActivity extends AppCompatActivity {

    public static final String EXTRA_ASSET_ID = "extra_asset_id";

    private View rootDetail;
    private TextView tvDetailName;
    private TextView tvDetailStatusBadge;
    private TextView tvDetailInventoryNumber;
    private TextView tvDetailCategory;
    private TextView tvDetailLocation;
    private TextView tvDetailMacAddress;
    private TextView tvDetailVendor;
    private TextView tvDetailBorrowedInfo;
    private TextView tvDetailNotes;
    private MaterialButton btnBorrowOrReturn;
    private LoanLogAdapter loanLogAdapter;
    private final List<LoanLog> loanLogs = new ArrayList<>();
    private PrefsManager prefsManager;

    private long assetId = -1L;
    private Asset currentAsset;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asset_detail);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        prefsManager = new PrefsManager(this);

        rootDetail = findViewById(R.id.rootDetail);
        tvDetailName = findViewById(R.id.tvDetailName);
        tvDetailStatusBadge = findViewById(R.id.tvDetailStatusBadge);
        tvDetailInventoryNumber = findViewById(R.id.tvDetailInventoryNumber);
        tvDetailCategory = findViewById(R.id.tvDetailCategory);
        tvDetailLocation = findViewById(R.id.tvDetailLocation);
        tvDetailMacAddress = findViewById(R.id.tvDetailMacAddress);
        tvDetailVendor = findViewById(R.id.tvDetailVendor);
        tvDetailBorrowedInfo = findViewById(R.id.tvDetailBorrowedInfo);
        tvDetailNotes = findViewById(R.id.tvDetailNotes);
        btnBorrowOrReturn = findViewById(R.id.btnBorrowOrReturn);

        ListView lvLoanLog = findViewById(R.id.lvLoanLog);
        TextView tvEmptyLoanLog = findViewById(R.id.tvEmptyLoanLog);
        lvLoanLog.setEmptyView(tvEmptyLoanLog);
        loanLogAdapter = new LoanLogAdapter(this, loanLogs);
        lvLoanLog.setAdapter(loanLogAdapter);

        assetId = getIntent().getLongExtra(EXTRA_ASSET_ID, -1L);
        btnBorrowOrReturn.setOnClickListener(v -> handleBorrowOrReturnClick());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAssetDetails();
    }

    private void loadAssetDetails() {
        currentAsset = MainActivity.findAssetById(assetId);
        if (currentAsset == null) {
            Toast.makeText(this, "Gerät nicht gefunden", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        tvDetailName.setText(currentAsset.getName());
        AssetListAdapter.applyStatusBadgeStyle(this, tvDetailStatusBadge, currentAsset);

        tvDetailInventoryNumber.setText(String.format("%s %s",
                getString(R.string.label_inv_number_short), valueOrDash(currentAsset.getInventoryNumber())));
        tvDetailCategory.setText(String.format("%s %s",
                getString(R.string.label_category_short), valueOrDash(currentAsset.getCategory())));
        tvDetailLocation.setText(String.format("%s %s",
                getString(R.string.label_location_short), valueOrDash(currentAsset.getLocation())));
        tvDetailMacAddress.setText(String.format("%s %s",
                getString(R.string.label_mac_short), valueOrDash(currentAsset.getMacAddress())));
        tvDetailVendor.setText(String.format("%s %s",
                getString(R.string.label_vendor_short), valueOrDash(currentAsset.getVendor())));
        tvDetailNotes.setText(String.format("%s %s",
                getString(R.string.label_notes_short), valueOrDash(currentAsset.getNotes())));

        if (Asset.STATUS_AUSGELIEHEN.equals(currentAsset.getStatus())
                && !TextUtils.isEmpty(currentAsset.getBorrowedBy())) {
            tvDetailBorrowedInfo.setVisibility(View.VISIBLE);
            String since = !TextUtils.isEmpty(currentAsset.getBorrowedSince())
                    ? " (seit " + currentAsset.getBorrowedSince() + ")"
                    : "";
            tvDetailBorrowedInfo.setText(String.format("%s %s%s",
                    getString(R.string.label_borrowed_short),
                    currentAsset.getBorrowedBy(),
                    since));
        } else {
            tvDetailBorrowedInfo.setVisibility(View.GONE);
        }

        updateActionButton();

        loanLogs.clear();
        loanLogs.addAll(MainActivity.getLoanLogsForAsset(assetId));
        loanLogAdapter.notifyDataSetChanged();
    }

    private void updateActionButton() {
        if (Asset.STATUS_AUSGELIEHEN.equals(currentAsset.getStatus())) {
            btnBorrowOrReturn.setEnabled(true);
            btnBorrowOrReturn.setText(R.string.btn_return_asset);
        } else if (Asset.STATUS_DEFEKT.equals(currentAsset.getStatus())) {
            btnBorrowOrReturn.setEnabled(false);
            btnBorrowOrReturn.setText("Defekt – Ausleihe gesperrt");
        } else {
            btnBorrowOrReturn.setEnabled(true);
            btnBorrowOrReturn.setText(R.string.btn_borrow_asset);
        }
    }

    private void handleBorrowOrReturnClick() {
        if (currentAsset == null) {
            return;
        }
        if (Asset.STATUS_AUSGELIEHEN.equals(currentAsset.getStatus())) {
            MainActivity.returnAsset(currentAsset.getId(), prefsManager.getAdminCode());
            loadAssetDetails();
            Snackbar.make(rootDetail, "Rückgabe verbucht.", Snackbar.LENGTH_SHORT).show();
        } else if (Asset.STATUS_VERFUEGBAR.equals(currentAsset.getStatus())) {
            showBorrowDialog();
        }
    }

    private void showBorrowDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_borrow_asset, null);
        TextInputEditText etBorrowerName = dialogView.findViewById(R.id.etBorrowerName);
        TextInputEditText etBorrowComment = dialogView.findViewById(R.id.etBorrowComment);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_borrow_title)
                .setView(dialogView)
                .setPositiveButton("Ausleihen", null)
                .setNegativeButton("Abbrechen", null)
                .create();

        dialog.setOnShowListener(d -> {
            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(v -> {
                String borrower = etBorrowerName.getText() != null
                        ? etBorrowerName.getText().toString().trim()
                        : "";
                String comment = etBorrowComment.getText() != null
                        ? etBorrowComment.getText().toString().trim()
                        : "";

                if (TextUtils.isEmpty(borrower)) {
                    etBorrowerName.setError("Bitte Namen oder Kürzel des Ausleihers eingeben");
                    etBorrowerName.requestFocus();
                    return;
                }

                MainActivity.borrowAsset(currentAsset.getId(), borrower, comment, prefsManager.getAdminCode());
                dialog.dismiss();
                loadAssetDetails();
                Snackbar.make(rootDetail, "Ausgeliehen an " + borrower, Snackbar.LENGTH_SHORT).show();
            });
        });

        dialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_edit_asset) {
            Intent intent = new Intent(this, AddEditAssetActivity.class);
            intent.putExtra(AddEditAssetActivity.EXTRA_ASSET_ID, assetId);
            startActivity(intent);
            return true;
        }
        if (id == R.id.action_delete_asset) {
            confirmDeleteAsset();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void confirmDeleteAsset() {
        if (currentAsset == null) {
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle("Gerät löschen")
                .setMessage("Soll \"" + currentAsset.getName() + "\" wirklich gelöscht werden?")
                .setPositiveButton("Löschen", (dialog, which) -> {
                    MainActivity.deleteAsset(currentAsset.getId());
                    Toast.makeText(this, "Gerät gelöscht", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .setNegativeButton("Abbrechen", null)
                .show();
    }

    private String valueOrDash(String value) {
        return !TextUtils.isEmpty(value) ? value : getString(R.string.not_specified);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
