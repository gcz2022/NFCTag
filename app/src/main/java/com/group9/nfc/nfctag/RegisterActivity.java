package com.group9.nfc.nfctag;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;
import connection.client.Client;

public class RegisterActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
    }

    public void register(View view) {
        final EditText username = (EditText) findViewById(R.id.name);
        final EditText pwd1 = (EditText) findViewById(R.id.password_edit1);
        final EditText pwd2 = (EditText) findViewById(R.id.password_edit2);
        final RadioGroup type = (RadioGroup) findViewById(R.id.user_type);
        Button register = (Button) findViewById(R.id.register);
        register.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (username.getText().toString().equals("")) {
                    dialog("请输入用户名");
                    username.requestFocus();
                } else if (pwd1.getText().toString().length() < 3) {
                    dialog("密码太短请重新输入");
                    username.requestFocus();
                } else if (!pwd1.getText().toString().equals(pwd2.getText().toString())) {
                    dialog("两次密码输入不同");
                } else {
                    final int user_type = (type.getCheckedRadioButtonId() == R.id.radioButton ? 1 : 0);
                    Toast.makeText(RegisterActivity.this, String.valueOf(user_type), Toast.LENGTH_LONG).show();
                    Client.Response response = new Client.AsnyRequest() {
                        public Client.Response getResponse() {
                            return Client.getClient().register(username.getText().toString(), pwd1.getText().toString(), user_type);
                        }
                    }.post();

                    if (response.getResult().equals("success")) {
                        Intent intent = new Intent(RegisterActivity.this, SignInActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        final MyDialog dialog1 = new MyDialog(RegisterActivity.this, R.style.MyDialog,response.getErrorMsg());
                        MyDialog.ListenerThree listenerThree = new MyDialog.ListenerThree() {
                            @Override
                            public void onClick(View view) {
                                switch (view.getId()) {
                                    case R.id.dialog_button_ok:
                                        Toast.makeText(RegisterActivity.this, "ok", Toast.LENGTH_LONG).show();
                                        dialog1.dismiss();
                                        break;
                                    case R.id.dialog_button_cancle:
                                        Toast.makeText(RegisterActivity.this, "cancel", Toast.LENGTH_LONG).show();
                                        dialog1.dismiss();
                                        break;
                                }
                            }
                        };
                        dialog1.SetListener(listenerThree);
                        dialog1.show();
                    }
                }
            }
        });

    }

    /**
     * dialog 弹出一个警告窗口 提示错误信息。
     */
    public void dialog(String ErrorMsg) {
        final MyDialog dialog1 = new MyDialog(RegisterActivity.this, R.style.MyDialog,ErrorMsg);
        MyDialog.ListenerThree listenerThree = new MyDialog.ListenerThree() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.dialog_button_ok:
                        Toast.makeText(RegisterActivity.this, "ok", Toast.LENGTH_LONG).show();
                        dialog1.dismiss();
                        break;
                    case R.id.dialog_button_cancle:
                        Toast.makeText(RegisterActivity.this, "cancel", Toast.LENGTH_LONG).show();
                        dialog1.dismiss();
                        break;
                }
            }
        };
        dialog1.SetListener(listenerThree);
        dialog1.show();
    }

}
