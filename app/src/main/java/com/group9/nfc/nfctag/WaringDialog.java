package com.group9.nfc.nfctag;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by yang on 15/7/23.
 */
public class WaringDialog extends Dialog implements View.OnClickListener {
    private Context context;
    private String ErrorMsg;
    private ListenerThree listener;
    private Button ok, cancel;
    private TextView waring;

    public WaringDialog(Context context, int theme, String Msg) {
        super(context, theme);
        this.context = context;
        this.ErrorMsg = Msg;
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
        this.setContentView(R.layout.waring_dialog);
        init();
    }

    public void init() {
        ok = (Button) findViewById(R.id.dialog_button_ok);
        waring = (TextView) findViewById(R.id.error_msg);
        ok.setOnClickListener(this);
        waring.setText(ErrorMsg);
    }

    @Override
    public void onClick(View v) {
        listener.onClick(v);
    }
}
