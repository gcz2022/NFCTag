package com.group9.nfc.nfctag;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class BillFragment extends Fragment {
    String comment, time, amount, type;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        comment = getArguments().getString("comment");
        time = getArguments().getString("time");
        amount = getArguments().getString("amount");
        type = getArguments().getString("type");

        return inflater.inflate(R.layout.fragment_bill, container, false);
    }

    public void onStart() {
        super.onStart();

        TextView commentView = (TextView) getView().findViewById(R.id.bill_comment);
        TextView amountView = (TextView) getView().findViewById(R.id.bill_amount);
        TextView timeView = (TextView) getView().findViewById(R.id.bill_time);
        ImageView imageView = (ImageView) getView().findViewById(R.id.bill_image);

        commentView.setText(comment);
        if (type.equals("from")) {
            amountView.setText("-" + amount);
            amountView.setTextColor(getResources().getColor(R.color.bill_pain));
            imageView.setImageDrawable(getResources().getDrawable(R.drawable.pain));
        } else {
            amountView.setText("+" + amount);
            amountView.setTextColor(getResources().getColor(R.color.bill_gain));
            imageView.setImageDrawable(getResources().getDrawable(R.drawable.gain));
        }
        timeView.setText(time);
    }
}

