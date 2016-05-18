package net.androidsrc.smssender;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.LinkedList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    String TAG = getClass().getSimpleName();

    @BindView(R.id.etMessage)
    EditText etMessage;
    @BindView(R.id.etPhoneNumber)
    EditText etPhoneNumber;
    @BindView(R.id.btnDone)
    Button btnDone;
    @BindView(R.id.btnContactsChooser)
    ImageButton btnContactsChooser;
    @BindView(R.id.spinnerWifiList)
    Spinner spinnerWifiList;
    @BindView(R.id.tvStatus)
    TextView tvStatus;

    String selectedItem = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        loadSavedData();
    }

    private void loadSavedData() {
        etMessage.setText(Utils.getPreference(this, Constants.KEY_MESSAGE));
        etPhoneNumber.setText(Utils.getPreference(this, Constants.KEY_PHONE_NUMBER));

        String status = Utils.getPreference(this, Constants.KEY_STATUS);
        if (status.isEmpty()) {
            status = "No Status";
        }
        tvStatus.setText(status);


        List<String> ssidList = new LinkedList<>();
        WifiManager wifiMgr = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        List<WifiConfiguration> data = wifiMgr.getConfiguredNetworks();
        ssidList.add(Constants.SPINNER_TITLE);
        for (WifiConfiguration d : data) {
            ssidList.add(d.SSID);
        }

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, ssidList);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerWifiList.setOnItemSelectedListener(this);
        int index = ssidList.indexOf(Utils.getPreference(this, Constants.KEY_WIFI_SPINNER));
        spinnerWifiList.setAdapter(dataAdapter);
        if (index >= 0 && index < ssidList.size())
            spinnerWifiList.setSelection(index);


    }

    @OnClick(R.id.btnContactsChooser)
    public void contactsChooser(View view){
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);  //should filter only contacts with phone numbers
        startActivityForResult(intent, Constants.CONTACT_PICKER_ID);
    }

    @OnClick(R.id.btnDone)
    public void submit(View v) {
        String phone = etPhoneNumber.getText().toString();
        String msg = etMessage.getText().toString();
        if (!phone.isEmpty()) {
            if (phone.length() >= 10) {
                Utils.setPreference(MainActivity.this, Constants.KEY_PHONE_NUMBER, phone);
            } else {
                etPhoneNumber.setError("Please enter valid 10 digit phone number");
            }

        } else {
            etPhoneNumber.setError("Please enter phone number");
        }
        if (!msg.isEmpty()) {
            Utils.setPreference(MainActivity.this, Constants.KEY_MESSAGE, msg);
        } else {
            etMessage.setError("Please enter Message to send");
        }

        if (!selectedItem.equalsIgnoreCase("")) {
            if (selectedItem.equalsIgnoreCase(Constants.SPINNER_TITLE)) {
                Toast.makeText(MainActivity.this, "Please select Wifi name", Toast.LENGTH_SHORT).show();
            } else {
                Utils.setPreference(this, Constants.KEY_WIFI_SPINNER, selectedItem);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request it is that we're responding to
        if (requestCode == Constants.CONTACT_PICKER_ID) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                // Get the URI that points to the selected contact
                Uri contactUri = data.getData();
                // We only need the NUMBER column, because there will be only one row in the result
                String[] projection = {ContactsContract.CommonDataKinds.Phone.NUMBER};

                // Perform the query on the contact to get the NUMBER column
                // We don't need a selection or sort order (there's only one result for the given URI)
                // CAUTION: The query() method should be called from a separate thread to avoid blocking
                // your app's UI thread. (For simplicity of the sample, this code doesn't do that.)
                // Consider using CursorLoader to perform the query.
                Cursor cursor = getContentResolver().query(contactUri, projection, null, null, null);
                cursor.moveToFirst();

                // Retrieve the phone number from the NUMBER column
                int column = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                String number = cursor.getString(column);
                etPhoneNumber.setText(number);
            }
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        selectedItem = parent.getItemAtPosition(position).toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        Log.d(TAG, "hello" + parent.getItemAtPosition(0).toString());
    }
}
