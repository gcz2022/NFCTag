package com.group9.nfc.nfctag;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;


public class SignInActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
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
    public void signIn(View view)
    {
        Intent intent;

        EditText account=(EditText)findViewById(R.id.account);
        EditText password=(EditText)findViewById(R.id.password);

        if(account.getText().toString().equals("admin"))
        {
            intent=new Intent(this, MainActivity.class);
            Toast.makeText(this, "Admin sign in succeed", Toast.LENGTH_LONG).show();
        }
        else
        {
            intent=new Intent(this, MainActivity2.class);
            Toast.makeText(this, "User sign in succeed", Toast.LENGTH_LONG).show();
        }

        startActivity(intent);
    }
}
