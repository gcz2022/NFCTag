package com.group9.nfc.nfctag;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Random;

import connection.client.Client;

/**
 * Created by yang on 15/7/17.
 */
public class PlaceholderFragment extends Fragment {
    private int account_balance;
    private int wallets;
    private TextView textAccountName;
    private TextView textAccountBalance;
    private Button buttonWallet;
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";
    private static final String ARG_BALANCE = "account_balance";

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static PlaceholderFragment newInstance(int sectionNumber) {
        PlaceholderFragment fragment = new PlaceholderFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public PlaceholderFragment() {
    }
    public String ranId(){
        String ans="";
        for (int i=0; i<4; i++){
            int ch = (int)Math.floor(Math.random()*1000) % 4;
            ans+=('a'+ch);
        }
        return ans;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        int select = getArguments().getInt(ARG_SECTION_NUMBER);
        View rootView = null;
        switch (select) {
            case 1:
                account_balance = new Client.AsnyRequest() {
                    public Client.Response getResponse() {
                        return Client.getClient().getUserInfo();
                    }
                }.post().helper.getUserBalance();
                rootView = inflater.inflate(R.layout.fragment_account, container, false);
                textAccountName = (TextView) rootView.findViewById(R.id.accountName);
                textAccountName.setText(Client.getClient().getUsername());
                textAccountBalance = (TextView) rootView.findViewById(R.id.accountBalance2);
                textAccountBalance.setText(String.valueOf(account_balance));
                break;
            case 2:
                rootView = inflater.inflate(R.layout.fragment_pay, container, false);
                break;
            case 3:
                rootView = inflater.inflate(R.layout.fragment_wallet, container, false);
                final View rootView_ = rootView;
                buttonWallet = (Button) rootView.findViewById(R.id.newWalletButton);
                buttonWallet.setOnClickListener(new Button.OnClickListener() {

                    public void onClick(View v) {
                        EditText newWallet = (EditText) rootView_.findViewById(R.id.newWallet);
                        EditText pwd = (EditText) rootView_.findViewById(R.id.AccountPwd);
                        EditText BalanceWallet = (EditText) rootView_.findViewById(R.id.WalletBalance);
                        EditText desc = (EditText) rootView_.findViewById(R.id.descriptionWallet);
                        String name = newWallet.getText().toString();
                        String rawVal = ranId();
                        String description = desc.getText().toString();
                        int balance = Integer.valueOf(BalanceWallet.getText().toString());
                        Client.Response response = Client.getClient().createWallet(name,rawVal,description,balance);
                        if(response.getResult().equals("success")){
                            // success
                            Log.i("app","success");
                        } else {
                            Log.i("app", response.getErrorMsg());
                        }
                    }
                });

                break;
            case 4:
                rootView = inflater.inflate(R.layout.fragment_settings, container, false);
                break;
        }
        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity2) activity).onSectionAttached(
                getArguments().getInt(ARG_SECTION_NUMBER));
    }
}

