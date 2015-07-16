package com.group9.nfc.nfctag;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import connection.client.Client;

public class SignInActivity extends ActionBarActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_sign_in, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void signIn(View view) {
        EditText accountView = (EditText) findViewById(R.id.account);
        EditText passwordView = (EditText) findViewById(R.id.password);
        final String username = accountView.getText().toString();
        final String password = passwordView.getText().toString();
        Client.Response response = new Client.AsnyRequest() {
            public Client.Response getResponse() {
                return Client.getClient().validate(username, password);
            }
        }.post();
        if (Client.getClient().isLogined()) {
            // Client.getClient().getUsername(); // 用该函数可以获取到登录用户的账号
            if (username.equals("admin")) {
                Intent intent = new Intent(this, MainActivity.class);
                Toast.makeText(this, "Admin sign in succeed", Toast.LENGTH_LONG).show();
                startActivity(intent);
            }
            Intent intent = new Intent(this, MainActivity2.class);
            Toast.makeText(this, "User :" + username + " sign in succeed", Toast.LENGTH_LONG).show();
            startActivity(intent);
        } else {
//            Toast.makeText(this, "input again", Toast.LENGTH_LONG).show();
            Toast.makeText(this, response.getErrorMsg(), Toast.LENGTH_LONG).show();
        }
    }
}
