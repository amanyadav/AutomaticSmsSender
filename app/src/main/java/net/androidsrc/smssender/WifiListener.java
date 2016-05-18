package net.androidsrc.smssender;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.telephony.SmsManager;
import android.util.Log;

/**
 * Created by aman on 10/5/16.
 */
public class WifiListener extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
        if (networkInfo != null) {
            Log.d("hello", "Type : " + networkInfo.getType() + "State : " + networkInfo.getState());


            if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {

                //get the different network states
                if (networkInfo.getState() == NetworkInfo.State.CONNECTED) {
                    WifiInfo info = wifiManager.getConnectionInfo();
                    String currentSSID = info.getSSID();
                    String savedSSID = Utils.getPreference(context, Constants.KEY_WIFI_SPINNER);
                    String todayDate = Utils.getTodayDate();
                    String lastSentDate = Utils.getPreference(context, Constants.KEY_LAST_SMS_DATE);
                    if (currentSSID.equalsIgnoreCase(savedSSID) && !todayDate.equalsIgnoreCase(lastSentDate)) {
                        sendSMS(context);
                    }
                }
            }
        }

    }

    private void sendSMS(Context con) {
        String number = Utils.getPreference(con, Constants.KEY_PHONE_NUMBER);
        String msg = Utils.getPreference(con, Constants.KEY_MESSAGE);
        if (!msg.isEmpty() && !number.isEmpty()) {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(number, null, msg, null, null);
            Utils.setPreference(con, Constants.KEY_LAST_SMS_DATE, Utils.getTodayDate());
            Utils.setPreference(con, Constants.KEY_STATUS, "last message was sent at " + Utils.getCurrentTime() + " on " + Utils.getTodayDate());

        } else {
            Utils.setPreference(con, Constants.KEY_STATUS, "There was problem with number or Msg");
        }
    }
}
