package com.westcoastlabs.quickwifi;

import android.util.Log;
/**
 * Created by ppl on 12/06/14.
 */
public class ExtractSSIDandKey {

    String extractedText;
    String[] textSplit;
    public ExtractSSIDandKey (String extractedText) {
        this.extractedText = extractedText;
        textSplit = extractedText.split(" |:|\\)");
    }

    public String getSSID() throws NoWifiInfoFoundException {
        String SSID = "";
        int SSIDLoc = -1;
        int nameLoc = -1;
        Log.i("Info Extraction: ", "Finding SSID");
        for (int x = 0; x < textSplit.length; x++) {
            //Possibly need contains cases
            if (textSplit[x].equalsIgnoreCase("SSID"))
                SSIDLoc = x;
            else if (textSplit[x].equalsIgnoreCase("Name"))
                nameLoc = x;
            Log.i("Info Extraction: ", textSplit[x]);
        }

        if (SSIDLoc == -1 && nameLoc == -1) {
            throw new NoWifiInfoFoundException("No SSID found");
        }

        if(nameLoc < SSIDLoc) {
            //SSID marker closer
            SSID = textSplit[SSIDLoc + 1]; //Rough guess, needs improvement.
        }
        else if ((SSIDLoc == -1 && nameLoc != -1) || (nameLoc > SSIDLoc)) {
            //No SSID marker just Name, or Name is closer than SSID
            SSID = textSplit[nameLoc + 1];//Rough guess, needs improvement.
        }
        return SSID;
    }

    public String getKey() throws NoWifiInfoFoundException {
        String key = "";
        int keyLoc = -1;
        int WEPLoc = -1;
        int passwordLoc = -1;

        for (int x = 0; x < textSplit.length; x++) {
            if (textSplit[x].equalsIgnoreCase("Key"))
                keyLoc = x;
            else if (textSplit[x].equalsIgnoreCase("Password"))
                passwordLoc = x;
            else if (textSplit[x].equalsIgnoreCase("WEP") || textSplit[x].equalsIgnoreCase("WPA") || textSplit[x].equalsIgnoreCase("WPA2") ||textSplit[x].equalsIgnoreCase("PSK"))
                WEPLoc = x;
            System.out.println(textSplit[x]);
        }

        int maxValue = Math.max(WEPLoc, Math.max(keyLoc, passwordLoc));
        if (maxValue == -1)
            throw new NoWifiInfoFoundException("No Key found");

        else
            key = textSplit[maxValue + 1]; //Rough guess, needs improvement.
        return key;
    }
}


