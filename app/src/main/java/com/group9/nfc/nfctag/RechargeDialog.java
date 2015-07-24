package com.group9.nfc.nfctag;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

/**
 * Created by yang on 15/7/22.
 */
public class RechargeDialog extends Dialog implements OnClickListener {
    private Context context;
    private String ErrorMsg;
    private ListenerThree listener;
    private Button ok, cancel;
    private EditText RMB;

    public EditText getRMB() {
        return RMB;
    }

    public RechargeDialog(Context context, int theme, String Msg) {
        super(context, theme);
        this.context = context;
        this.ErrorMsg = Msg;
    }

    public RechargeDialog(Context context, int theme) {
        super(context, theme);
        this.context = context;
    }

    public interface ListenerThree {
        void onClick(View view);
    }

    public void SetListener(ListenerThree listener) {
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.recharge_dialog);
        init();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    public void init() {
        ok = (Button) findViewById(R.id.dialog_button_ok);
        cancel = (Button) findViewById(R.id.dialog_button_cancle);
        RMB = (EditText) findViewById(R.id.recharge_edit);
        RMB.setHint(R.string.recharge_hint);
        ok.setOnClickListener(this);
        cancel.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        listener.onClick(v);
    }
}
