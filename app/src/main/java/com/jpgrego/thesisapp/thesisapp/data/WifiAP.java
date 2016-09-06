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

        if(scanResult.capabilities != null) {
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
        } else {
            wifiSecurityImageResource = R.drawable.wifi_security_unknown;
            this.securityLabel = "?";
        }

        this.ssid = scanResult.SSID;
        this.bssid = scanResult.BSSID;
        this.channel = WifiUtils.frequencyToChannel(scanResult.frequency);
        this.dbm = scanResult.level;
    }

    public void decrementVisibilityCounter() {
        visibilityCounter--;
    }

    public int getVisibilityCounter() {
        return visibilityCounter;
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
        return this.dbm - another.dbm;
    }
}
