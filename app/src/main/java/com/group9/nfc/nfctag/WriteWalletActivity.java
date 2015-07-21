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
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.group9.nfc.nfctag.R;

import java.nio.charset.Charset;

/**

 * Created by desolate on 15/5/19.
 */
public class WriteWalletActivity extends Activity {
    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;
    private IntentFilter[] tagFilters;
    private boolean writeMode = false;

    private EditText editText;
    private Button buttonWrite;

    private String wallet_rawVal;
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        Intent intent=getIntent();
        wallet_rawVal=intent.getStringExtra("wallet_rawVal");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activiry_write);

        editText = (EditText)findViewById(R.id.editText);
        editText.setText(wallet_rawVal);

        buttonWrite = (Button)findViewById(R.id.button);
        buttonWrite.setText("写入钱包标识符");
        buttonWrite.setOnClickListener(_tagWriter);

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter == null) {
            Toast.makeText(this, "The device doesn't support NFC.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        checkNFCEnabled();
        pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);

        tagFilters = new IntentFilter[] {tagDetected};
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        checkNFCEnabled();
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
        if (writeMode)
        {
            if (intent.getAction().equals(NfcAdapter.ACTION_TAG_DISCOVERED))
            {
                Tag detectedTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                writeTag(buildNdefMessage(), detectedTag);

                editText.setEnabled(true);
            }
        }
    }

    private final View.OnClickListener _tagWriter = new View.OnClickListener()
    {
        @Override
        public void onClick(View arg0)
        {
            if (editText.getText().toString().trim().length() == 0)
            {
                Toast.makeText(WriteWalletActivity.this, "The data to write is empty. Please fill it!",
                        Toast.LENGTH_LONG).show();
            }
            else
            {
                enableTagWriteMode();
            }
        }
    };

    boolean writeTag(NdefMessage message, Tag tag)
    {
        int size = message.toByteArray().length;

        try {
            Ndef ndef = Ndef.get(tag);
            if (ndef != null) {
                ndef.connect();

                if (!ndef.isWritable()) {
                    Toast.makeText(this, "Cannot write to this tag. This tag is read-only.", Toast.LENGTH_LONG).show();
                    return false;
                }

                if (ndef.getMaxSize() < size) {
                    Toast.makeText(this,
                            "Cannot write to this tag. Message size (" + size + " bytes) exceeds this tag's capacity of " + ndef.getMaxSize()
                                    + " bytes.", Toast.LENGTH_LONG).show();
                    return false;
                }

                ndef.writeNdefMessage(message);
                Toast.makeText(this, "钱包ID已写入芯片！", Toast.LENGTH_LONG).show();
                finish();
                return true;
            }

            Toast.makeText(this, "Cannot write to this tag. This tag does not support NDEF.", Toast.LENGTH_LONG).show();
            return false;

        } catch (Exception e) {
            Toast.makeText(this, "Cannot write to this tag due to an Exception.", Toast.LENGTH_LONG).show();
        }

        return false;
    }

    private NdefMessage buildNdefMessage()
    {
        String data = editText.getText().toString().trim();

        String mimeType = "application/com.group9.nfc.nfctag";

        byte[] mimeBytes = mimeType.getBytes(Charset.forName("UTF-8"));
        byte[] dataBytes = data.getBytes(Charset.forName("UTF-8"));
        byte[] id = new byte[0];

        NdefRecord record = new NdefRecord(NdefRecord.TNF_MIME_MEDIA, mimeBytes, id, dataBytes);
        NdefMessage message = new NdefMessage(new NdefRecord[]{record});

        return message;
    }


    private void enableTagWriteMode()
    {
        writeMode = true;
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, tagFilters, null);
        editText.setEnabled(false);
    }


    private void checkNFCEnabled()
    {
        boolean enable = nfcAdapter.isEnabled();
        if (!enable) {
            new AlertDialog.Builder(WriteWalletActivity.this).setTitle("NFC is turned off!").setMessage("Please enable the NFC").setCancelable(false)
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

