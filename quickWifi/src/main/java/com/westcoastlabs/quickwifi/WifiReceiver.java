package com.westcoastlabs.quickwifi;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by ppl on 16/06/14.
 */
public class WifiReceiver extends BroadcastReceiver {

    Toast last_toast = null;
    String last = "";
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        String scannedSSID = sp.getString("SSID", "");

        final String action = intent.getAction();

        //Log.i("onRecv action", action);
        ConnectivityManager conMan = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = conMan.getActiveNetworkInfo();
        if (netInfo != null && netInfo.getType() == ConnectivityManager.TYPE_WIFI) {
            Log.d("WifiReceiver", "Have Wifi Connection");


            final WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            final WifiInfo connectionInfo = wifiManager.getConnectionInfo();

            String SSID;
            if (connectionInfo != null) {
                SSID = connectionInfo.getSSID();
                Log.d("WifiReceiver", "SSID is " + SSID);
                Log.d("WifiReceiver", "Scanned SSID is " + scannedSSID);
                String compScanned = "\"" + scannedSSID + "\"";
                if (SSID.equalsIgnoreCase(compScanned)) {
                    Log.d("WifiReceiver", "Connected to " + scannedSSID);
                    Toast.makeText(context, "Connected to " + scannedSSID, Toast.LENGTH_LONG).show();
                    context.unregisterReceiver(this);
                    return;
                }
            } else
                return;
        }


        if (!(last_toast == null))
            last_toast.cancel();
        if (action.equals(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION)) {
            //Log.i("Supplicant"," change");
            SupplicantState supl_state=((SupplicantState)intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE));

            switch(supl_state){
                case AUTHENTICATING:Log.i("SupplicantState", "Authenticating...");
                    last_toast = Toast.makeText(context, "Authenticating...", Toast.LENGTH_SHORT);
                    last_toast.show();
                    break;
                case COMPLETED:Log.i("SupplicantState", "Connected");
                    break;
                case DISCONNECTED:Log.i("SupplicantState", "Disconnected");
                    if (last.equals("Group") || last.equals("Four")) {
                        Toast.makeText(context, "Failed to connect to " + scannedSSID + " Authentication problem", Toast.LENGTH_LONG).show();
                        context.unregisterReceiver(this);
                        return;
                    }
                    break;
                case FOUR_WAY_HANDSHAKE:Log.i("SupplicantState", "FOUR_WAY_HANDSHAKE");
                    last_toast = Toast.makeText(context, "Handshake...", Toast.LENGTH_SHORT);
                    last_toast.show();
                    last = "Four";
                    break;
                case GROUP_HANDSHAKE:Log.i("SupplicantState", "GROUP_HANDSHAKE");
                    last_toast = Toast.makeText(context, "Handshake...", Toast.LENGTH_SHORT);
                    last_toast.show();
                    last = "Group";
                    break;

            }
            int supl_error=intent.getIntExtra(WifiManager.EXTRA_SUPPLICANT_ERROR, -1);
            if(supl_error==WifiManager.ERROR_AUTHENTICATING){
                Toast.makeText(context, "Error connecting, key incorrect", Toast.LENGTH_LONG).show();
                context.unregisterReceiver(this);
                return;
            }
            return;
        }
        last_toast = Toast.makeText(context, "Connecting...", Toast.LENGTH_SHORT);
        last_toast.show();
    }
}