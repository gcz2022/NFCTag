package com.group9.nfc.nfctag;

import connection.client.Client;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.net.PasswordAuthentication;

import javax.xml.validation.Validator;

public class SignInActivity extends ActionBarActivity {
    Boolean result = false;

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
        AccessClient accessClient = new AccessClient(this);
        accessClient.start();
        try {
            accessClient.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return;
        }
        if (Client.getClient().isLogined()) {
            EditText account = (EditText) findViewById(R.id.account);
            if (account.getText().toString().equals("admin")) {
                Intent intent = new Intent(this, MainActivity.class);
                Toast.makeText(this, "Admin sign in succeed", Toast.LENGTH_LONG).show();

                intent.putExtra("account", account.getText().toString());
                startActivity(intent);
            } else {
                Intent intent = new Intent(this, MainActivity2.class);
                Toast.makeText(this, "User :" + account.getText().toString() + " sign in succeed", Toast.LENGTH_LONG).show();

                intent.putExtra("account", account.getText().toString());

                startActivity(intent);
            }
        } else {
            Toast.makeText(this, "input again", Toast.LENGTH_LONG).show();
        }
//        if(account.getText().toString().equals("admin"))
//        {
//            intent=new Intent(this, MainActivity.class);
//            Toast.makeText(this, "Admin sign in succeed", Toast.LENGTH_LONG).show();
//        }
//        else
//        {
//            intent=new Intent(this, MainActivity2.class);
//            Toast.makeText(this, "User sign in succeed", Toast.LENGTH_LONG).show();
//        }
    }
}

class AccessClient extends Thread {
    SignInActivity activity;

    public AccessClient(SignInActivity activity) {
        this.activity = activity;
    }

    public void run() {
        Intent intent;
        Client client = Client.getClient();
        EditText account = (EditText) activity.findViewById(R.id.account);
        EditText password = (EditText) activity.findViewById(R.id.password);
        String username = account.getText().toString();
        String pwd = password.getText().toString();
        Log.i("Android", "username is " + username + "  password is " + pwd);
        client.validate(username, pwd);
    }
}