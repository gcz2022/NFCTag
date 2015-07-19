package com.group9.nfc.nfctag;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import connection.client.Client;

/**
 * Created by desolate on 15/5/19.
 */


public class ReadTagActivity extends Activity
{
    private static String TAG = ReadTagActivity.class.getSimpleName();


    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;
    IntentFilter[] tagFilters;

    Button returnnns;
    private TextView textView;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read);

        textView = (TextView)findViewById(R.id.textView);
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        returnnns=(Button)findViewById(R.id.returnnns);
        returnnns.setOnClickListener(new View.OnClickListener() {
                                         @Override
                                         public void onClick(View v)
                                         {
                                             if(Client.getClient().getUsername().equals("admin"))
                                             {
                                                 Intent intent=new Intent(v.getContext(), MainActivity.class);
                                                 intent.putExtra("read", "true");
                                                 intent.putExtra("customerId", textView.getText().toString());
                                                 startActivity(intent);
                                             }
                                             else
                                                 startActivity(new Intent(v.getContext(), MainActivity2.class));
                                         }
                                     }

        );

        if (nfcAdapter == null) {
            Toast.makeText(this, "The device doesn't support NFC.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        checkNFCEnabled();

        pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        IntentFilter ndetected = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);

        try {
            ndetected.addDataType("application/com.group9.nfc.nfctag");
        }
        catch (IntentFilter.MalformedMimeTypeException e)  {
            throw new RuntimeException("Can not add MIME type", e);
        }

        tagFilters = new IntentFilter[]{ndetected};
    }


    @Override
    protected void onResume()
    {
        super.onResume();
        checkNFCEnabled();

        if (getIntent().getAction() != null) {
            if (getIntent().getAction().equals(NfcAdapter.ACTION_NDEF_DISCOVERED)) {
                NdefMessage[] msgs = getNdefMessagesFromIntent(getIntent());
                NdefRecord record = msgs[0].getRecords()[0];
                byte[] payload = record.getPayload();

                String payloadString = new String(payload);

                textView.setText(payloadString);



            }
        }
    }

    @Override
    protected void onPause()
    {
        super.onPause();

        nfcAdapter.disableForegroundDispatch(this);
    }

    @Override
    protected void onNewIntent(Intent intent)
    {

        if (intent.getAction().equals(NfcAdapter.ACTION_NDEF_DISCOVERED))
        {
            NdefMessage[] msgs = getNdefMessagesFromIntent(intent);
            confirmDisplayedContentOverwrite(msgs[0]);

        } else if (intent.getAction().equals(NfcAdapter.ACTION_TAG_DISCOVERED)) {
            Toast.makeText(this, "This NFC tag has no NDEF data.", Toast.LENGTH_LONG).show();
        }
    }

    NdefMessage[] getNdefMessagesFromIntent(Intent intent)
    {
        NdefMessage[] msgs = null;
        String action = intent.getAction();
        if (action.equals(NfcAdapter.ACTION_TAG_DISCOVERED) || action.equals(NfcAdapter.ACTION_NDEF_DISCOVERED)) {
            Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            if (rawMsgs != null) {
                msgs = new NdefMessage[rawMsgs.length];
                for (int i = 0; i < rawMsgs.length; i++) {
                    msgs[i] = (NdefMessage) rawMsgs[i];
                }

            } else {
                // Unknown tag type
                byte[] empty = new byte[]{};
                NdefRecord record = new NdefRecord(NdefRecord.TNF_UNKNOWN, empty, empty, empty);
                NdefMessage msg = new NdefMessage(new NdefRecord[]{record});
                msgs = new NdefMessage[]{msg};
            }

        } else {
            Log.e(TAG, "Unknown intent.");
            finish();
        }
        return msgs;
    }

    private void confirmDisplayedContentOverwrite(final NdefMessage msg)
    {
        final String data = textView.getText().toString().trim();

        new AlertDialog.Builder(this).setTitle("New tag found!").setMessage("Do you wanna show the content of this tag?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        String payload = new String(msg.getRecords()[0].getPayload());
                        textView.setText((payload));

                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int id)
            {
                textView.setText(data);
                dialog.cancel();
            }
        }).show();
    }

    private void checkNFCEnabled()
    {
        boolean enable = nfcAdapter.isEnabled();
        if (!enable) {
            new AlertDialog.Builder(ReadTagActivity.this).setTitle("NFC is turned off!").setMessage("Please enable the NFC").setCancelable(false)
                    .setPositiveButton("Set", new DialogInterface.OnClickListener()
            {
                @Override
                    public void onClick(DialogInterface dialog, int id)
                {
                    startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
                }
            }).create().show();
        }
    }
}
