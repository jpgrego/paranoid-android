package com.jpgrego.thesisapp.thesisapp.data;

import android.net.wifi.ScanResult;
import android.support.annotation.NonNull;
import com.jpgrego.thesisapp.thesisapp.R;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by jpgrego on 24-08-2016.
 */
public class WifiAP implements Comparable<WifiAP> {

    private final static Pattern WIFI_SECURITY_PATTERN =
            java.util.regex.Pattern.compile(".*?(IBSS)|.*?(WPA2)|.*?(WPA)|.*?(WEP)");

    public final String securityLabel, ssid, bssid;
    public final int wifiSecurityImageResource, channel, dbm;
    private int visibilityCounter = 3;

    public WifiAP(@NonNull ScanResult scanResult) {
        final Matcher matcher;

        matcher = WIFI_SECURITY_PATTERN.matcher(scanResult.capabilities);
        if (matcher.find()) {
            this.securityLabel = matcher.group(2);

            switch (this.securityLabel) {
                case "IBSS":
                    this.wifiSecurityImageResource = R.drawable.wifi_security_adhoc;
                    break;

                case "WPA2":
                case "WPA":
                case "WEP":
                    this.wifiSecurityImageResource = R.drawable.wifi_security_wep_wpa;
                    break;

                default:
                    this.wifiSecurityImageResource = R.drawable.wifi_security_unknown;
            }
        } else {
            wifiSecurityImageResource = R.drawable.wifi_security_open;
            this.securityLabel = "-";
        }

        this.ssid = scanResult.SSID;
        this.bssid = scanResult.BSSID;
        this.channel = frequencyToChannel(scanResult.frequency);
        this.dbm = scanResult.level;
    }

    public void decrementVisibilityCounter() {
        visibilityCounter--;
    }

    public int getVisibilityCounter() {
        return visibilityCounter;
    }

    /*
     * Thought out by myself... please excuse me for any mistake. I wanted an alternative to
     * creating an array or an HashMap, though not sure if this is better in terms of performance!
     * (jpgrego)
     */
    private int frequencyToChannel(int frequency) {
        if (frequency <= 2484) {                             // 802.11 b/g/n (2.4 GHz)
            return frequency % 2412 / 5 + 1;
        } else if (frequency <= 4980) {                      // 802.11 a/h/j/n/ac (5 GHz)
            return frequency % 4915 / 5 + 183;
        } else if (frequency <= 5080) {
            return frequency % 5035 / 5 + 7;
        } else if (frequency <= 5320) {
            return frequency % 5170 / 5 + 34;
        } else {
            return frequency % 5500 / 5 + 100;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        WifiAP wifiAP = (WifiAP) o;

        if (bssid.equals(wifiAP.bssid)) {
            visibilityCounter = 3;
            return true;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return bssid.hashCode();
    }

    @Override
    public int compareTo(@NonNull WifiAP another) {
        return another.dbm - this.dbm;
    }
}
