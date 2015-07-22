package com.group9.nfc.nfctag;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by yang on 15/7/22.
 */
public class MyDialog extends Dialog implements OnClickListener {
    private Context context;
    private String ErrorMsg;
    private ListenerThree listener;
    private Button ok, cancel;
    private TextView hint;

    public MyDialog(Context context, int theme, String Msg) {
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
        this.setContentView(R.layout.mydialog);
        init();
    }

    public void init() {
        ok = (Button) findViewById(R.id.dialog_button_ok);
        cancel = (Button) findViewById(R.id.dialog_button_cancle);
        hint = (TextView) findViewById(R.id.error_msg);
        ok.setOnClickListener(this);
        cancel.setOnClickListener(this);
        hint.setText(ErrorMsg);
    }

    @Override
    public void onClick(View v) {
        listener.onClick(v);
    }
}
