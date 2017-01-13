package com.jpgrego.thesisapp.thesisapp.data;

import android.net.wifi.ScanResult;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.util.Log;
import com.jpgrego.thesisapp.thesisapp.R;
import com.jpgrego.thesisapp.thesisapp.utils.WifiUtils;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by jpgrego on 24-08-2016.
 */
public final class WifiAP implements Comparable<WifiAP>, Parcelable {

    private final static Pattern WIFI_SECURITY_PATTERN =
            java.util.regex.Pattern.compile(".*?(IBSS)|.*?(WPA2)|.*?(WPA)|.*?(WEP)");

    private final String securityLabel, ssid, bssid;
    private final int wifiSecurityImageResource, channel, frequency, dbm;
    private final AtomicInteger visibilityCounter;
    private final AtomicLong timestamp;

    private WifiAP(Parcel in) {
        securityLabel = in.readString();
        ssid = in.readString();
        bssid = in.readString();
        wifiSecurityImageResource = in.readInt();
        channel = in.readInt();
        frequency = in.readInt();
        dbm = in.readInt();
        visibilityCounter = new AtomicInteger(in.readInt());
        timestamp = new AtomicLong(in.readLong());
    }

    private WifiAP(final ScanResult scanResult) {
        final Matcher matcher;

        if(scanResult.capabilities != null) {
            matcher = WIFI_SECURITY_PATTERN.matcher(scanResult.capabilities);
            if (matcher.find()) {
                this.securityLabel = matcher.group(2);

                if(this.securityLabel == null) {
                    Log.e(this.getClass().getName(),
                            "Weird WiFi capabilities found, please investigate!");
                    Log.e(this.getClass().getName(), "capabilities: " + scanResult.capabilities);
                    this.wifiSecurityImageResource = R.drawable.wifi_security_unknown;
                } else {
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
        this.frequency = scanResult.frequency;
        this.dbm = scanResult.level;
        this.visibilityCounter = new AtomicInteger(3);
        this.timestamp = new AtomicLong(scanResult.timestamp);
    }

    public static WifiAP fromScanResult(final ScanResult scanResult) {
        return new WifiAP(scanResult);
    }

    public static final Creator<WifiAP> CREATOR = new Creator<WifiAP>() {
        @Override
        public WifiAP createFromParcel(Parcel in) {
            return new WifiAP(in);
        }

        @Override
        public WifiAP[] newArray(int size) {
            return new WifiAP[size];
        }
    };

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if(!(o instanceof WifiAP)) return false;

        WifiAP wifiAP = (WifiAP) o;

        if (bssid != null && bssid.equals(wifiAP.bssid)) {
            visibilityCounter.set(3);
            timestamp.set(wifiAP.timestamp.get());
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(securityLabel);
        parcel.writeString(ssid);
        parcel.writeString(bssid);
        parcel.writeInt(wifiSecurityImageResource);
        parcel.writeInt(channel);
        parcel.writeInt(frequency);
        parcel.writeInt(dbm);
        parcel.writeInt(visibilityCounter.get());
        parcel.writeLong(timestamp.get());
    }

    public String getSecurityLabel() {
        return securityLabel;
    }

    public String getSsid() {
        return ssid;
    }

    public String getBssid() {
        return bssid;
    }

    public int getWifiSecurityImageResource() {
        return wifiSecurityImageResource;
    }

    public int getChannel() {
        return channel;
    }

    public int getFrequency() {
        return frequency;
    }

    public int getDbm() {
        return dbm;
    }

    public AtomicInteger getVisibilityCounter() {
        return visibilityCounter;
    }

    public long getTimeSinceLastSeen() {
        return SystemClock.elapsedRealtime() * 1000 - this.timestamp.get();
    }


}
