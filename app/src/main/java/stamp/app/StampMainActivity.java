package stamp.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.bitcoin.core.ECKey;
import com.google.bitcoin.crypto.DeterministicKey;
import com.google.bitcoin.crypto.HDKeyDerivation;
import com.google.bitcoin.crypto.MnemonicCode;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class StampMainActivity extends ActionBarActivity implements View.OnClickListener {

    Button scanButton;
    EditText editText;

    static final int SCAN_QR_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stamp_main);

        String walletSeed = getWalletSeed();

        scanButton = (Button) findViewById(R.id.scan_button);
        scanButton.setOnClickListener(this);

        editText = (EditText) findViewById(R.id.editText);

        editText.setText(walletSeed);
    }

    private String getWalletSeed() {
        String walletSeed = getSharedPreferences("bip39", MODE_PRIVATE)
                .getString("wallet-seed", "");

        if(walletSeed.equals("")) {

            SecureRandom sr = new SecureRandom();
            byte[] rndBytes = new byte[32];
            sr.nextBytes(rndBytes);

            try {
                MnemonicCode mc = new MnemonicCode();
                List<String> mn = mc.toMnemonic(rndBytes);
                StringBuilder seed = new StringBuilder();
                for(String s : mn) {
                    seed.append(s);
                    seed.append(" ");
                }

                walletSeed = seed.toString().trim();

                SharedPreferences.Editor e = getSharedPreferences("bip39", MODE_PRIVATE).edit();
                e.putString("wallet-seed", seed.toString().trim());
                e.commit();
            } catch(Exception e) {
                throw new RuntimeException(e);
            }
        }
        return walletSeed;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.stamp_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        Intent intent = new Intent(this, ScanQRCodeActivity.class);
        startActivityForResult(intent, SCAN_QR_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == SCAN_QR_CODE) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                // The user picked a contact.
                // The Intent's data Uri identifies which contact was selected.
                editText.setText(data.getStringExtra("result"));
                // Do something with the contact here (bigger example below)
                processQRRequest(data.getStringExtra("result"));
            }
        }
    }

    private void processQRRequest(String data) {

        try {

            String[] params = data.split("|");
            if(params.length < 3)
                return;


            String cmd = params[0];
            //String service = params[1];
            //String post_back = params[2];

            if(cmd.equals("mpk")) {
                String[] seed = getWalletSeed().split(" ");
                MnemonicCode mc = new MnemonicCode();
                byte[] rnd = mc.toEntropy(Arrays.asList(seed));

                DeterministicKey ekprv = HDKeyDerivation.createMasterPrivateKey(rnd);

                editText.setText(ekprv.serializePubB58());
            }
        }
        catch(Exception e) {
            // TODO
        }
    }
}
