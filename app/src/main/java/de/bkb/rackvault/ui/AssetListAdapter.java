package de.bkb.rackvault.ui;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;

import java.util.List;

import de.bkb.rackvault.R;
import de.bkb.rackvault.model.Asset;

/**
 * Eigener {@link ArrayAdapter} zur Darstellung der Inventar-Geräte in einer {@code ListView}.
 */
public class AssetListAdapter extends ArrayAdapter<Asset> {

    public AssetListAdapter(@NonNull Context context, @NonNull List<Asset> assets) {
        super(context, 0, assets);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.item_asset, parent, false);
        }

        Asset asset = getItem(position);
        if (asset == null) {
            return view;
        }

        TextView tvName = view.findViewById(R.id.tvItemName);
        TextView tvStatusBadge = view.findViewById(R.id.tvItemStatusBadge);
        TextView tvSubtitle = view.findViewById(R.id.tvItemSubtitle);
        TextView tvBorrower = view.findViewById(R.id.tvItemBorrower);

        tvName.setText(asset.getName());
        tvSubtitle.setText(String.format("%s · %s · %s",
                asset.getInventoryNumber(),
                asset.getCategory(),
                asset.getLocation()));

        applyStatusBadgeStyle(getContext(), tvStatusBadge, asset);

        if (Asset.STATUS_AUSGELIEHEN.equals(asset.getStatus())
                && asset.getBorrowedBy() != null
                && !asset.getBorrowedBy().trim().isEmpty()) {
            tvBorrower.setVisibility(View.VISIBLE);
            tvBorrower.setText(String.format("Ausgeliehen an: %s", asset.getBorrowedBy()));
        } else {
            tvBorrower.setVisibility(View.GONE);
        }

        return view;
    }

    /**
     * Färbt den Status-Badge-TextView passend zum Gerätestatus (Grün/Orange/Rot).
     */
    public static void applyStatusBadgeStyle(@NonNull Context context,
                                             @NonNull TextView badgeView,
                                             @NonNull Asset asset) {
        badgeView.setText(asset.getStatusDisplayName());

        int bgColorRes;
        int textColorRes;
        if (Asset.STATUS_AUSGELIEHEN.equals(asset.getStatus())) {
            bgColorRes = R.color.status_borrowed_bg;
            textColorRes = R.color.status_borrowed_text;
        } else if (Asset.STATUS_DEFEKT.equals(asset.getStatus())) {
            bgColorRes = R.color.status_defect_bg;
            textColorRes = R.color.status_defect_text;
        } else {
            bgColorRes = R.color.status_available_bg;
            textColorRes = R.color.status_available_text;
        }

        int bgColor = ContextCompat.getColor(context, bgColorRes);
        int textColor = ContextCompat.getColor(context, textColorRes);
        ViewCompat.setBackgroundTintList(badgeView, ColorStateList.valueOf(bgColor));
        badgeView.setTextColor(textColor);
    }
}
