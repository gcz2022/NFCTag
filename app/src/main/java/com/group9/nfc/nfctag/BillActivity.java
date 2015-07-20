package com.group9.nfc.nfctag;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import connection.client.Client;
import connection.json.JSONArray;
import connection.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class BillActivity extends ActionBarActivity
        implements Spinner.OnItemSelectedListener {
    List<Fragment> addedFragments = new ArrayList<>();
    JSONArray wallets;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bill);
        Spinner spinner = (Spinner) findViewById(R.id.bill_spinner);
        Client.Response walletsResponse = new Client.AsnyRequest() {
            public Client.Response getResponse() {
                return Client.getClient().getWallets();
            }
        }.post();

        if (walletsResponse.getResult().equals("success")) {
            wallets = walletsResponse.json.getJSONArray("wallets");
            List<String> options = new ArrayList<>();
            options.add("帐户账单");
            for (int i = 0; i < wallets.length(); i++) {
                String name = wallets.getJSONObject(i).getString("name");
                options.add(name);
            }
            ArrayAdapter<String> adapter =
                    new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, options);
            spinner.setAdapter(adapter);
            spinner.setOnItemSelectedListener(this);
        }

        int walletId = getIntent().getIntExtra("walletId", 0);
        if (walletId == 0) {
            spinner.setSelection(0);
        } else {
            for (int i = 0; i < wallets.length(); i++) {
                if (wallets.getJSONObject(i).getInt("id") == walletId) {
                    spinner.setSelection(i + 1);
                    break;
                }
            }
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (position == 0) { // user account
            reload(0);
        } else {
            reload(wallets.getJSONObject(position - 1).getInt("id"));
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    public void reload(final int walletId) {
        Client.Response response;
        if (walletId == 0) { // user account
            response = new Client.AsnyRequest() {
                public Client.Response getResponse() {
                    return Client.getClient().getBills();
                }
            }.post();
        } else { // user wallet
            response = new Client.AsnyRequest() {
                public Client.Response getResponse() {
                    return Client.getClient().getWalletBills(walletId);
                }
            }.post();
        }

        if (response.getResult().equals("success")) {
            // begin transaction
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            // clear all added fragments
            for (Fragment addedFragment : addedFragments) {
                transaction.remove(addedFragment);
            }
            addedFragments.clear();

            // add bills
            JSONArray bills = response.json.getJSONArray("bills");
            for (int i = 0; i < bills.length(); i++) {
                JSONObject bill = bills.getJSONObject(i);
                BillFragment fragment = new BillFragment();
                Bundle bundle = new Bundle();
                bundle.putString("comment", bill.getString("comment"));
                bundle.putString("time", bill.getString("time"));
                bundle.putString("amount", bill.getString("amount"));
                bundle.putString("type", bill.getString("type"));
                fragment.setArguments(bundle);
                transaction.add(R.id.bill_container, fragment);
                addedFragments.add(fragment);
            }
            transaction.commit();
        } else { // fail to get bills
            findViewById(R.id.bill_error_view).setVisibility(View.VISIBLE);
            TextView errorMsgView = (TextView) findViewById(R.id.bill_fetch_error_msg);
            errorMsgView.setText(response.getErrorMsg());
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
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
