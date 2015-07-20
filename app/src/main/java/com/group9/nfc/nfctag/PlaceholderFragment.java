package com.group9.nfc.nfctag;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.method.DigitsKeyListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.lang.annotation.Retention;

import connection.client.Client;
import connection.json.JSONArray;
import connection.json.JSONObject;

/**
 * Created by yang on 15/7/17.
 */
public class PlaceholderFragment extends Fragment {
    private static final String ARG_SECTION_NUMBER = "section_number";
    private static String rawVal = "";
    private static final String ERROR_MSG_NULL_BALANCE = "输入金额";

    private Button RechargeBalance;
    private MainActivity2 mActivity;
    private int accountBalance;
    private TextView textWallets;
    private TextView textAccountName;
    private TextView textAccountBalance;
    private TextView textWalletName;
    private TextView textWalletBalance;
    private TextView hintWalletBalance;
    private Button buttonWallet;
    private View rootView = null;


    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static PlaceholderFragment newInstance(int sectionNumber, Activity activity) {
        PlaceholderFragment fragment = new PlaceholderFragment();
        fragment.mActivity = (MainActivity2) activity;
        Bundle args = new Bundle();
        rawVal = "";
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public static PlaceholderFragment newInstance(int sectionNumber, String text, Activity activity) {
        PlaceholderFragment fragment = new PlaceholderFragment();
        fragment.mActivity = (MainActivity2) activity;
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        fragment.rawVal = text;
        return fragment;
    }

    /**
     * 返回一个随机字符串代表钱包的标识符rawVal
     *
     * @return string
     */
    public String ranId() {
        String ans = "";
        for (int i = 0; i < 4; i++) {
            int ch = (int) Math.floor(Math.random() * 1000) % 4;
            ans += ('a' + ch);
        }
        return ans;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        int select = getArguments().getInt(ARG_SECTION_NUMBER);
        /**
         * 根据不同的select 来创建不同的fragment
         */
        switch (select) {
            case 1:
                rootView = inflater.inflate(R.layout.fragment_account, container, false);
                RechargeBalance = (Button) rootView.findViewById(R.id.recharge);
                RechargeBalance.setOnClickListener(new Button.OnClickListener() {
                    public void onClick(View v) {
                        dialog();
                    }
                });
                accountBalance = new Client.AsnyRequest() {
                    public Client.Response getResponse() {
                        return Client.getClient().getUserInfo();
                    }
                }.post().helper.getUserBalance();
                textAccountName = (TextView) rootView.findViewById(R.id.accountName);
                textAccountName.setText(Client.getClient().getUsername());
                textAccountBalance = (TextView) rootView.findViewById(R.id.accountBalance2);
                textAccountBalance.setText(String.valueOf(accountBalance));
                Client.Response response = new Client.AsnyRequest() {
                    public Client.Response getResponse() {
                        return Client.getClient().getWallets();
                    }
                }.post();
                if (response.getResult().equals("success")) {
                    JSONArray wallets = response.json.getJSONArray("wallets");
                    textWallets = (TextView) rootView.findViewById(R.id.accountWalletNum);
                    textWallets.setText(String.valueOf(wallets.length()));
                    LinearLayout layout = (LinearLayout) rootView.findViewById(R.id.account);
                    for (int i = 0; i < wallets.length(); i++) {
                        final JSONObject wallet = wallets.getJSONObject(i);
                        final int walletId = wallet.getInt("id");
                        final LinearLayout ly = (LinearLayout) inflater.inflate(R.layout.wallet, null).findViewById(R.id.addwallet);
                        textWalletName = (TextView) ly.findViewById(R.id.WalletName);
                        textWalletName.setText(wallet.getString("name"));
                        textWalletBalance = (TextView) ly.findViewById(R.id.WalletBalance);
                        textWalletBalance.setText(String.valueOf(wallet.getInt("balance")));
                        ly.findViewById(R.id.deleteWallet).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
                                builder.setTitle("虚拟钱包");
                                builder.setMessage("确认删除钱包么");
                                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Toast.makeText(mActivity, "delete success", Toast.LENGTH_SHORT).show();
                                        int currentWallets = Integer.valueOf(textWallets.getText().toString()) - 1;
                                        textWallets.setText(String.valueOf(currentWallets));
                                        int currentBalance = Integer.valueOf(textAccountBalance.getText().toString()) +
                                                Integer.valueOf(String.valueOf(wallet.getInt("balance")));
                                        textAccountBalance.setText(String.valueOf(currentBalance));
                                        ly.setVisibility(View.GONE);
                                        new Client.AsnyRequest() {
                                            public Client.Response getResponse() {
                                                return Client.getClient().deleteWallet(walletId);
                                            }
                                        }.post();
                                    }
                                });
                                //    设置一个NegativeButton
                                builder.setNegativeButton("取消", null);
                                //    显示出该对话框
                                builder.show();

                            }
                        });
                        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        lp.setMargins(0, 20, 0, 0);
                        layout.addView(ly, lp);

                    }
                } else {
                    response.getErrorMsg();
                }
                break;
            case 2:
                rootView = inflater.inflate(R.layout.fragment_pay, container, false);
                accountBalance = new Client.AsnyRequest() {
                    public Client.Response getResponse() {
                        return Client.getClient().getUserInfo();
                    }
                }.post().helper.getUserBalance();
                textAccountName = (TextView) rootView.findViewById(R.id.accountName);
                textAccountName.setText(Client.getClient().getUsername());
                textAccountBalance = (TextView) rootView.findViewById(R.id.accountBalance);
                textAccountBalance.setText(String.valueOf(accountBalance));
                if (!rawVal.equals("")) {
                    Client.Response response1 = new Client.AsnyRequest() {
                        public Client.Response getResponse() {
                            return Client.getClient().getItemInfo(rawVal);
                        }
                    }.post();
                    if (response1.getResult().equals("success")) {
                        JSONObject itemInfo = response1.json.getJSONObject("itemInfo");
                        final String price = itemInfo.getString("price");
                        final String id = itemInfo.getString("id");
                        final String name = itemInfo.getString("name");
                        final String desc = itemInfo.getString("description");
                        TextView item_name = (TextView) rootView.findViewById(R.id.item_name);
                        TextView item_price = (TextView) rootView.findViewById(R.id.item_price);
                        TextView item_desc = (TextView) rootView.findViewById(R.id.item_description);
                        item_name.setText(name);
                        item_price.setText(String.valueOf(price));
                        item_desc.setText(desc);
                        final NewEditText number = (NewEditText) rootView.findViewById(R.id.layout);
                        number.getText().toString();
                        Button item_buy = (Button) rootView.findViewById(R.id.item_buy);
                        item_buy.setOnClickListener(new Button.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
                                int total = Integer.valueOf(price) * Integer.valueOf(number.getText().toString());
                                builder.setMessage("总共消费：" + String.valueOf(total) + "元");
                                builder.setTitle("提示");
                                builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Client.Response response2 = new Client.AsnyRequest() {
                                            @Override
                                            public Client.Response getResponse() {
                                                return Client.getClient().buyItem(Integer.valueOf(id), Integer.valueOf(number.getText().toString()));
                                            }
                                        }.post();
                                        if (response2.getResult().equals("success")) {
                                            Toast.makeText(mActivity, "购买成功", Toast.LENGTH_LONG);
                                            mActivity.onNavigationDrawerItemSelected(0);
                                            mActivity.getSupportActionBar().setTitle(getString(R.string.title_section2_1));
                                            mActivity.mNavigationDrawerFragment.selectItem(0);
                                        }
                                    }
                                });
                                builder.create().show();
                            }
                        });
                    } else {
                        dialog("No Item Found !");
                    }
                }
                break;
            case 3:
                rootView = inflater.inflate(R.layout.fragment_wallet, container, false);
                final View rootView_ = rootView;
                accountBalance = new Client.AsnyRequest() {
                    public Client.Response getResponse() {
                        return Client.getClient().getUserInfo();
                    }
                }.post().helper.getUserBalance();
                textAccountName = (TextView) rootView.findViewById(R.id.accountName);
                textAccountName.setText(Client.getClient().getUsername());
                textAccountBalance = (TextView) rootView.findViewById(R.id.accountBalance);
                textAccountBalance.setText(String.valueOf(accountBalance));
                hintWalletBalance = (EditText) rootView.findViewById(R.id.WalletBalance);
                hintWalletBalance.setHint("输入1~" + String.valueOf(accountBalance) + "元");
                buttonWallet = (Button) rootView.findViewById(R.id.newWalletButton);
                buttonWallet.setOnClickListener(new Button.OnClickListener() {

                    public void onClick(View v) {
                        EditText newWallet = (EditText) rootView_.findViewById(R.id.newWallet);
                        EditText pwd = (EditText) rootView_.findViewById(R.id.AccountPwd);
                        EditText BalanceWallet = (EditText) rootView_.findViewById(R.id.WalletBalance);
                        EditText desc = (EditText) rootView_.findViewById(R.id.descriptionWallet);
                        if (BalanceWallet.getText().toString().equals("")) {
                            dialog(ERROR_MSG_NULL_BALANCE);
                            BalanceWallet.requestFocus();
                        } else {
                            final String name = newWallet.getText().toString();
                            final String rawVal = ranId();
                            final String description = desc.getText().toString();
                            final int balance = Integer.valueOf(BalanceWallet.getText().toString());
                            Client.Response response = new Client.AsnyRequest() {
                                public Client.Response getResponse() {
                                    return Client.getClient().createWallet(name, rawVal, description, balance);
                                }
                            }.post();
                            if (response.getResult().equals("success")) {
                                // success
                                Toast.makeText(mActivity, "success", Toast.LENGTH_LONG).show();
                                mActivity.onNavigationDrawerItemSelected(0);
                                mActivity.getSupportActionBar().setTitle(getString(R.string.title_section2_1));
                                mActivity.mNavigationDrawerFragment.selectItem(0);
                                Log.i("app", "success");
                            } else {
                                Log.i("app", response.getErrorMsg());
                            }
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

    /**
     * dialog 弹出一个警告窗口 提示错误信息。
     *
     * @param ErrorMsg
     */
    public void dialog(String ErrorMsg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        builder.setMessage(ErrorMsg);
        builder.setTitle("提示");
        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    /**
     * 输入充值金额
     */
    public void dialog() {
        final Context context = mActivity;
        //定义1个文本输入框
        final EditText AccountRecharge = new EditText(mActivity);
        AccountRecharge.setKeyListener(new DigitsKeyListener(false, true));
        //创建对话框
        new AlertDialog.Builder(context)
                .setTitle("请输入金额")//设置对话框标题
                .setIcon(android.R.drawable.ic_dialog_info)//设置对话框图标
                .setView(AccountRecharge)//为对话框添加要显示的组件
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {//设置对话框[肯定]按钮
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Client.Response response = new Client.AsnyRequest() {
                            public Client.Response getResponse() {
                                return Client.getClient().recharge(Integer.valueOf(AccountRecharge.getText().toString()));
                            }
                        }.post();
                        if (response.getResult().equals("success")) {
                            accountBalance += Integer.valueOf(AccountRecharge.getText().toString());
                            textAccountBalance = (TextView) rootView.findViewById(R.id.accountBalance2);
                            textAccountBalance.setText(String.valueOf(accountBalance));
                            Toast.makeText(mActivity, "充值成功", Toast.LENGTH_LONG).show();
                        }
                    }
                })
                .setNegativeButton("取消", null)//设置对话框[否定]按钮
                .show();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity2) activity).onSectionAttached(
                getArguments().getInt(ARG_SECTION_NUMBER));
    }
}

