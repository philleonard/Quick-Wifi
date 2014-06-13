package com.westcoastlabs.quickwifi;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.net.wifi.ScanResult;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import java.util.List;

/**
 * Created by ppl on 12/06/14.
 */
public class Connect extends AsyncTask<Void, Void, Void>{

    WifiManager wm;
    Context context;
    String SSID;
    String key;
    MainActivity main;

    public Connect(MainActivity main, String SSID, String key) {
        this.main = main;
        context = main.getApplicationContext();
        this.key = key;
        this.SSID = SSID;
        wm =  (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
    }

    @Override
    protected Void doInBackground(Void... params) {
        connect();
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        Animation fadeOutAnimation = AnimationUtils.loadAnimation(main.getApplicationContext(), R.anim.fade_out);
        fadeOutAnimation.setFillAfter(false);
        main.grey.startAnimation(fadeOutAnimation);

        main.capture.setVisibility(View.VISIBLE);
        main.load.setVisibility(View.INVISIBLE);
        main.prog.setVisibility(View.INVISIBLE);
        main.load.setText("");
        super.onPostExecute(aVoid);
    }

    public void connect() {
        if (!wm.isWifiEnabled()) {
            wm.setWifiEnabled(true);
            wm.startScan();
            FindAP();
        }
        else {
            FindAP();
        }
    }

    public void FindAP () {
        List<ScanResult> wifiScanList = wm.getScanResults();
        String ssids[];
        ssids = new String[wifiScanList.size()];
        for(int i = 0; i < wifiScanList.size(); i++){
            ssids[i] = ((wifiScanList.get(i)).SSID);
        }

        int[] distance = new int[ssids.length];
        int minHam = Integer.MAX_VALUE;
        String bestSSIDmatch = "";
        for(int x = 0; x < ssids.length; x++) {
            //Create confidence list
            distance[x] = getHammingDistance(ssids[x], SSID);
            if (distance[x] < minHam) {
                minHam = distance[x];
                bestSSIDmatch = ssids[x];
            }
        }


        Log.i("Connection", "Best SSID match is \"" + bestSSIDmatch + "\"");

        WifiConfiguration wc = new WifiConfiguration();
        wc.SSID = "\"" + bestSSIDmatch + "\"";
        wc.preSharedKey = "\"" + key + "\"";
        /*wc.status = WifiConfiguration.Status.ENABLED;
        wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
        wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
        wc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        wc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
        wc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
        wc.allowedProtocols.set(WifiConfiguration.Protocol.RSN);*/
        // connect to and enable the connection
        wc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
        int netId = wm.addNetwork(wc);
        wm.enableNetwork(netId, true);

        List<WifiConfiguration> list = wm.getConfiguredNetworks();
        for( WifiConfiguration i : list ) {
            if(i.SSID != null && i.SSID.equals("\"" + bestSSIDmatch + "\"")) {
                wm.disconnect();
                wm.enableNetwork(i.networkId, true);
                wm.reconnect();

                break;
            }
        }
    }

    public static int getHammingDistance(String sequence1, String sequence2) {
        char[] s1 = sequence1.toCharArray();
        char[] s2 = sequence2.toCharArray();

        int shorter = Math.min(s1.length, s2.length);
        int longest = Math.max(s1.length, s2.length);

        int result = 0;
        for (int i=0; i<shorter; i++) {
            if (s1[i] != s2[i]) result++;
        }

        result += longest - shorter;

        return result;
    }
}
