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
    private ActionBarActivity mContext;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.activity_sign_in);
        if (Client.getClient().isLogined()){
            if(Client.getClient().getUsername().equals("admin")){
                Intent intent = new Intent(this, MainActivity.class);
                Toast.makeText(this, "Admin sign in succeed", Toast.LENGTH_LONG).show();
                intent.putExtra("account", "admin");
                mContext.startActivity(intent);
                mContext.finish();
            }   else
            {
                Intent intent = new Intent(this, MainActivity2.class);
                Toast.makeText(this, "User :" + Client.getClient().getUsername() + " sign in succeed", Toast.LENGTH_LONG).show();
                mContext.startActivity(intent);
                mContext.finish();
            }
        }
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
        if (response.getResult().equals("success")) {
            if (username.equals("admin")) {
                Intent intent = new Intent(this, MainActivity.class);
                Toast.makeText(this, "Admin sign in succeed", Toast.LENGTH_LONG).show();
                intent.putExtra("account", "admin");
                mContext.startActivity(intent);
                mContext.finish();
            } else {
                Intent intent = new Intent(this, MainActivity2.class);
                Toast.makeText(this, "User :" + username + " sign in succeed", Toast.LENGTH_LONG).show();
                mContext.startActivity(intent);
                mContext.finish();
            }
        } else {
//            Toast.makeText(this, "input again", Toast.LENGTH_LONG).show();
            Toast.makeText(this, response.getErrorMsg(), Toast.LENGTH_LONG).show();
        }
    }
}
