package com.group9.nfc.nfctag;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class BillFragment extends Fragment {
    String comment, date, amount;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        comment = getArguments().getString("comment");
        date = getArguments().getString("date");
        amount = getArguments().getString("amount");

        return inflater.inflate(R.layout.fragment_bill, container, false);
    }

    public void onStart() {
        super.onStart();

        TextView commentView = (TextView) getView().findViewById(R.id.bill_comment);
        TextView amountView = (TextView) getView().findViewById(R.id.bill_amount);
        commentView.setText(comment);
        amountView.setText("-" + amount);
        amountView.setTextColor(getResources().getColor(R.color.bill_pain));
    }
}

