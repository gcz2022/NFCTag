package com.group9.nfc.nfctag;

import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import connection.client.Client;
import connection.json.JSONArray;
import connection.json.JSONObject;


public class BillActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bill);

        int walletId = getIntent().getIntExtra("walletId", 0);
        if (walletId == 0) { // user account
            // todo
        } else { // user wallet

        }

        Client.Response response = new Client.AsnyRequest() {
            public Client.Response getResponse() {
                return Client.getClient().getBills();
            }
        }.post();
        if (response.getResult().equals("success")) {
            // start add bill fragments
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            JSONArray bills = response.json.getJSONArray("bills");
            for (int i = 0; i < bills.length(); i++) {
                JSONObject bill = bills.getJSONObject(i);
                BillFragment fragment = new BillFragment();
                Bundle bundle = new Bundle();
                bundle.putString("comment", bill.getString("comment"));
                bundle.putString("date", "即将实现");
                bundle.putString("amount", bill.getString("amount"));

                fragment.setArguments(bundle);
                transaction.add(R.id.bill_container, fragment, "bill-" + i);
            }
            transaction.commit();
        } else { // fail to get bills
            findViewById(R.id.fail_to_load_bill_prompt).setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
}
