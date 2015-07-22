package com.group9.nfc.nfctag;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.nio.Buffer;
import java.util.Properties;
import java.util.Random;

import connection.client.Client;
import connection.json.JSONArray;
import connection.json.JSONObject;

public class MainActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    private String account;

    private TextView accountTextView;
    private TextView balanceTextView;
    private TextView balanceTitleTextView;
    private TextView customerId;
    private TextView amount;
    private TextView goodsName;
    private TextView goodsDescription;
    private TextView unitPrice;
    private TextView goodsAmount;

    private LinearLayout myAccount;
    private LinearLayout goodsIn;
    private LinearLayout customerBuy;
    private int itemNum;
    private LinearLayout adminHasItemLayout;

    private NavigationDrawerFragment mNavigationDrawerFragment;

    private CharSequence mTitle;

    void initViewPointer() {
        myAccount = (LinearLayout) findViewById(R.id.myAccount);
        goodsIn = (LinearLayout) findViewById(R.id.goodsIn);
        customerBuy=(LinearLayout) findViewById(R.id.customerBuy);

        //用户名
        accountTextView = (TextView) findViewById(R.id.accountName);
        accountTextView.setText(account);
        accountTextView.setTextSize(25);

        //余额
        balanceTextView = (TextView) findViewById(R.id.balance);
        String balanceNum=getBalance(); //= String.valueOf(response.helper.getUserBalance());
        balanceTextView.setText(balanceNum);
        balanceTextView.setTextSize(25);

        //商品入库
        goodsName=(TextView) findViewById(R.id.goodsName);
        goodsDescription=(TextView) findViewById(R.id.goodsDescription);
        unitPrice=(TextView) findViewById(R.id.unitPrice);
        goodsAmount=(TextView) findViewById(R.id.goodsAmount);

        //用户消费
        customerId=(TextView) findViewById(R.id.customerId);
        amount=(TextView) findViewById(R.id.amount);

    }
    public String getBalance()
    {
        Client.Response response = new Client.AsnyRequest() {
            public Client.Response getResponse() {
                return Client.getClient().getUserInfo();
            }
        }.post();
        String balanceNum = String.valueOf(response.helper.getUserBalance());
        return balanceNum;
    }
    public int getItemNum()
    {
        Client.Response response = new Client.AsnyRequest() {
            public Client.Response getResponse() {
                return Client.getClient().getItems();
            }
        }.post();

        int res=response.json.getJSONArray("items").length();
        return res;
    }
    public void readCustomer(View view)
    {
        Toast.makeText(this, "正在读取nfc数据……", Toast.LENGTH_LONG).show();
    }
    //商品入库
    public void createItem(View view)
    {
        final String generateResult=generateGoodsId();
        final String goodsNameStr=goodsName.getText().toString();
        final String goodsDescriptionStr=goodsDescription.getText().toString();
        final String unitPriceStr=unitPrice.getText().toString();
        final String goodsAmountStr=goodsAmount.getText().toString();

        if(goodsNameStr.equals("")||goodsDescriptionStr.equals("")||unitPriceStr.equals("")||goodsAmountStr.equals(""))
        {
            if(goodsNameStr.equals(""))
                Toast.makeText(this, "请填写商品名称！", Toast.LENGTH_LONG).show();
            if(goodsDescriptionStr.equals(""))
                Toast.makeText(this, "请填写商品描述！", Toast.LENGTH_LONG).show();
            if(unitPriceStr.equals(""))
                Toast.makeText(this, "请填写商品价格！", Toast.LENGTH_LONG).show();
            if(goodsAmountStr.equals(""))
                Toast.makeText(this, "请填写商品数量！", Toast.LENGTH_LONG).show();

        }
        else
        {
            Intent intent = new Intent(this, WriteTagActivity.class);
            intent.putExtra("goodsId", generateResult);
            intent.putExtra("goodsName", goodsNameStr);
            intent.putExtra("goodsDescription", goodsDescriptionStr);
            intent.putExtra("unitPrice", unitPriceStr);

            Client.Response response = new Client.AsnyRequest() {
                public Client.Response getResponse() {
                    return Client.getClient().createItem(generateResult, goodsNameStr, goodsDescriptionStr, Integer.parseInt(unitPriceStr), Integer.parseInt(goodsAmountStr));
                }
            }.post();
            if (response.getResult().equals("success")) {
                Toast.makeText(this, "仓库中已记载该商品！", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "商品信息存入出错！", Toast.LENGTH_LONG).show();
            }
            startActivity(intent);
        }
    }
    public char generateCharacter(Random random)
    {
        char res;
        int randomInt = random.nextInt(36);
        if (randomInt>25)
            res=(char)((randomInt-26)+'0');
        else
            res=((char) (random.nextInt(26) + 'A'));

        return res;
    }
    public String generateGoodsId()
    {
        Random random=new Random();
        StringBuffer buffer=new StringBuffer();
        for(int i=0; i<8; i++)
        {
            buffer.append(generateCharacter(random));
        }
        return buffer.toString();
    }
    //用户消费
    public void charge(View view)
    {
        if(customerId.getText().toString().isEmpty())
        {
            Toast.makeText(this, "请填写商品ID！", Toast.LENGTH_LONG).show();
        }
        else if(amount.getText().toString().isEmpty())
        {
            Toast.makeText(this, "请填写商品数量！", Toast.LENGTH_LONG).show();
        }
        else
        {

            try
            {
                int amountInt=Integer.parseInt(amount.getText().toString());
                Client.Response response = new Client.AsnyRequest(){
                    public Client.Response getResponse(){
                        return Client.getClient().charge(customerId.getText().toString(), Integer.parseInt(amount.getText().toString()));
                    }
                }.post();
                if(response.getResult().equals("success"))
                {
                    Toast.makeText(this, "收款成功！", Toast.LENGTH_LONG).show();
                }
                else
                {
                    // error
                    Toast.makeText(this, response.getErrorMsg(), Toast.LENGTH_LONG).show();    // get error message
                }
            }
            catch (Exception e)
            {
                Toast.makeText(this, "请填写正确的商品数量！", Toast.LENGTH_LONG).show();
            }
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Intent intent = getIntent();


        account = intent.getStringExtra("account");

        super.onCreate(savedInstanceState);
        mNavigationDrawerFragment = new NavigationDrawerFragment();
        mNavigationDrawerFragment.setType("admin");
        setContentView(R.layout.activity_main);

        initViewPointer();

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);

        mTitle = getTitle();
        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        if(intent.getStringExtra("read")!=null&&intent.getStringExtra("read").equals("true"))
        {
            onNavigationDrawerItemSelected(2);
            customerId.setText(intent.getStringExtra("customerId"));
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {

        super.onRestoreInstanceState(savedInstanceState);

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {

        super.onSaveInstanceState(outState);

    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, PlaceholderFragment.newInstance(position + 1))
                .commit();
    }
    public void refresh()
    {
        Client.Response response = new Client.AsnyRequest() {
            public Client.Response getResponse() {
                return Client.getClient().getItems();
            }
        }.post();
        if(response.getResult().equals("success"))
        {
            JSONArray items = response.json.getJSONArray("items");
            int i;
            for (i = 0; i < (5 > items.length() ? items.length() : 5); i++) {
                try {
                    findViewById(R.id.class.getField("adminHasItem_" + i).getInt(R.id.class.newInstance())).setVisibility(View.VISIBLE);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                }
            }
            for(; i<5; i++)
            {
                try {
                    findViewById(R.id.class.getField("adminHasItem_" + i).getInt(R.id.class.newInstance())).setVisibility(View.GONE);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    public void addItems()
    {

        Client.Response response = new Client.AsnyRequest() {
            public Client.Response getResponse() {
                return Client.getClient().getItems();
            }
        }.post();
        if(response.getResult().equals("success"))
        {
            JSONArray items=response.json.getJSONArray("items");
            int i;
            for(i=0; i<(5>items.length()?items.length():5); i++)
            {

                JSONObject item=items.getJSONObject(items.length()-1-i);
                if(item==null)
                    break;

                String goodsNameStr="goodsName_"+i;
                String goodsDescriptionStr="goodsDescription_"+i;
                String goodsPriceStr="goodsUnitPrice_"+i;
                String goodsAmountStr="goodsAmount_"+i;

                TextView goodsNameView= null;
                TextView goodsDescriptionView=null;
                TextView goodsPriceView=null;
                TextView goodsAmountView=null;
                try
                {
                    goodsNameView = (TextView)findViewById(R.id.class.getField(goodsNameStr).getInt(R.class.newInstance()));
                    goodsDescriptionView = (TextView)findViewById(R.id.class.getField(goodsDescriptionStr).getInt(R.class.newInstance()));
                    goodsPriceView = (TextView)findViewById(R.id.class.getField(goodsPriceStr).getInt(R.class.newInstance()));
                    goodsAmountView = (TextView)findViewById(R.id.class.getField(goodsAmountStr).getInt(R.class.newInstance()));


                }
                catch (IllegalAccessException e) {

                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                }

                goodsNameView.setText(item.getString("name"));
                goodsDescriptionView.setText(item.getString("description"));
                goodsPriceView.setText(item.getString("price"));
                goodsAmountView.setText(item.getString("inventory"));
                final int id=item.getInt("id");
                final boolean delete;
                try {
                    findViewById(R.id.class.getField("adminHasItem_" + i).getInt(R.id.class.newInstance())).setVisibility(View.VISIBLE);
                    findViewById(R.id.class.getField("delete" + i).getInt(R.id.class.newInstance())).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                            builder.setTitle("移除商品");
                            builder.setMessage("确认移除该商品？");
                            builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Toast.makeText(MainActivity.this, "delete success", Toast.LENGTH_SHORT).show();

                                    new Client.AsnyRequest() {
                                        public Client.Response getResponse() {

                                            return Client.getClient().deleteItem(id);

                                        }
                                    }.post();
                                    addItems();
                                }
                            });
                            //    设置一个NegativeButton
                            builder.setNegativeButton("取消", null);
                            //    显示出该对话框
                            builder.show();

                        }
                    });
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                }

            }
            for(; i<5; i++)
            {
                try {
                    findViewById(R.id.class.getField("adminHasItem_" + i).getInt(R.id.class.newInstance())).setVisibility(View.GONE);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    public void onSectionAttached(int number)
    {
        switch (number)
        {
            case 1:
                String balanceNum=getBalance();

                balanceTextView.setText(balanceNum);

                addItems();
                refresh();
                myAccount.setVisibility(View.VISIBLE);
                goodsIn.setVisibility(View.GONE);

                customerBuy.setVisibility(View.GONE);

                mTitle = getString(R.string.title_section1);
                break;
            case 2:
                myAccount.setVisibility(View.GONE);
                goodsIn.setVisibility(View.VISIBLE);
                customerBuy.setVisibility(View.GONE);

                mTitle = getString(R.string.title_section2);
                break;
            case 3:
                myAccount.setVisibility(View.GONE);
                goodsIn.setVisibility(View.GONE);
                customerBuy.setVisibility(View.VISIBLE);

                mTitle = getString(R.string.title_section3);
                break;
            case 4:
                mTitle = getString(R.string.title_section4);
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SignInActivity.class));
            Client.getClient().logout();
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

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

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            int select = getArguments().getInt(ARG_SECTION_NUMBER);
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);//activity_main, container, false);

            return rootView;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((MainActivity) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }
    }
}
