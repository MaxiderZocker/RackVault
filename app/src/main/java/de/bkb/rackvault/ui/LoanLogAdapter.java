package de.bkb.rackvault.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import de.bkb.rackvault.R;
import de.bkb.rackvault.model.LoanLog;

/**
 * Eigener {@link ArrayAdapter} zur Darstellung der Ausleih- und Status-Historie.
 */
public class LoanLogAdapter extends ArrayAdapter<LoanLog> {

    public LoanLogAdapter(@NonNull Context context, @NonNull List<LoanLog> logs) {
        super(context, 0, logs);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.item_loan_log, parent, false);
        }

        LoanLog log = getItem(position);
        if (log == null) {
            return view;
        }

        TextView tvActionAndTime = view.findViewById(R.id.tvLogActionAndTime);
        TextView tvActorInfo = view.findViewById(R.id.tvLogActorInfo);
        TextView tvComment = view.findViewById(R.id.tvLogComment);

        tvActionAndTime.setText(String.format("%s · %s", log.getTimestamp(), formatAction(log.getAction())));

        String borrower = (log.getBorrower() != null && !log.getBorrower().trim().isEmpty())
                ? log.getBorrower()
                : "—";
        String admin = (log.getAdminCode() != null && !log.getAdminCode().trim().isEmpty())
                ? log.getAdminCode()
                : "ADM";
        tvActorInfo.setText(String.format("Person: %s · Admin: %s", borrower, admin));

        if (log.getComment() != null && !log.getComment().trim().isEmpty()) {
            tvComment.setVisibility(View.VISIBLE);
            tvComment.setText(String.format("„%s“", log.getComment().trim()));
        } else {
            tvComment.setVisibility(View.GONE);
        }

        return view;
    }

    private String formatAction(String action) {
        if (LoanLog.ACTION_AUSLEIHE.equals(action)) {
            return "Ausleihe";
        }
        if (LoanLog.ACTION_RUECKGABE.equals(action)) {
            return "Rückgabe";
        }
        if (LoanLog.ACTION_DEFEKT_MELDUNG.equals(action)) {
            return "Defekt gemeldet";
        }
        if (LoanLog.ACTION_REPARIERT.equals(action)) {
            return "Repariert";
        }
        return action != null ? action : "";
    }
}
