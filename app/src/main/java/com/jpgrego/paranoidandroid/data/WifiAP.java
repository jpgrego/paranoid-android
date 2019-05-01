package com.jpgrego.paranoidandroid.data;

import android.database.Cursor;
import android.net.wifi.ScanResult;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import com.jpgrego.paranoidandroid.R;
import com.jpgrego.paranoidandroid.utils.WifiUtils;
import com.jpgrego.paranoidandroid.db.DatabaseContract.WifiAPEntry;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by jpgrego on 24-08-2016.
 */
public final class WifiAP implements Comparable<WifiAP>, Parcelable {

    //private final static Pattern WIFI_SECURITY_PATTERN =
    //        java.util.regex.Pattern.compile(".*?(IBSS)|.*?(WPA2)|.*?(WPA)|.*?(WEP)");
    //private final static Pattern WIFI_SECURITY_PATTERN =
    //        java.util.regex.Pattern.compile("(IBSS(?=-))|(WPA2(?=-))|(WPA(?=-))|(WEP(?=-))");

    private final String securityLabel, lastSecurityLabel, ssid, bssid;
    private final int wifiSecurityImageResource, channel, frequency, dbm;
    private final AtomicInteger visibilityCounter;
    private volatile long timestamp;

    private WifiAP(Parcel in) {
        securityLabel = in.readString();
        lastSecurityLabel = in.readString();
        ssid = in.readString();
        bssid = in.readString();
        wifiSecurityImageResource = in.readInt();
        channel = in.readInt();
        frequency = in.readInt();
        dbm = in.readInt();
        visibilityCounter = new AtomicInteger(in.readInt());
        timestamp = in.readLong();
    }

    private WifiAP(final ScanResult scanResult) {

        this.securityLabel = getSecurityFromCapabilities(scanResult.capabilities);
        this.lastSecurityLabel = "";
        this.wifiSecurityImageResource = getImageResourceFromLabel(this.securityLabel);

        this.ssid = scanResult.SSID;
        this.bssid = scanResult.BSSID;
        this.channel = WifiUtils.frequencyToChannel(scanResult.frequency);
        this.frequency = scanResult.frequency;
        this.dbm = scanResult.level;
        this.visibilityCounter = new AtomicInteger(3);
        this.timestamp = TimeUnit.MICROSECONDS.toMillis(scanResult.timestamp);
    }

    private WifiAP(final String ssid, final String bssid, final int channel,
                   final String securityLabel, final String lastSecurityLabel,
                   final int frequency) {
        this.ssid = ssid;
        this.bssid = bssid;
        this.channel = channel;
        this.securityLabel = securityLabel;
        this.lastSecurityLabel = lastSecurityLabel;
        this.wifiSecurityImageResource = getImageResourceFromLabel(this.securityLabel);
        this.frequency = frequency;
        this.dbm = 0;
        this.visibilityCounter = null;
    }

    public static WifiAP fromScanResult(final ScanResult scanResult) {
        return new WifiAP(scanResult);
    }

    // this assumes the cursor is a valid query and in the desired row
    public static WifiAP fromCursor(final Cursor cursor) {
        if(cursor.isClosed()) return null;

        final String ssid = cursor.getString(cursor.getColumnIndex(WifiAPEntry.SSID_COLUMN));
        final String bssid = cursor.getString(cursor.getColumnIndex(WifiAPEntry.BSSID_COLUMN));
        final int channel = cursor.getInt(cursor.getColumnIndex(WifiAPEntry.CHANNEL_COLUMN));
        final String securityLabel = cursor.getString(
                cursor.getColumnIndex(WifiAPEntry.SECURITY_COLUMN));
        final String lastSecurityLabel = cursor.getString(
                cursor.getColumnIndex(WifiAPEntry.LAST_SECURITY_COLUMN));
        final int frequency = cursor.getInt(cursor.getColumnIndex(WifiAPEntry.FREQUENCY_COLUMN));

        return new WifiAP(ssid, bssid, channel, securityLabel, lastSecurityLabel, frequency);
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

        final WifiAP wifiAP = (WifiAP) o;

        if (bssid != null && bssid.equals(wifiAP.bssid)) {
            visibilityCounter.set(3);
            timestamp = wifiAP.timestamp;
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
        if(this.equals(another)) return 0;
        else {
            final int dbmDiff = another.dbm - this.dbm;
            return dbmDiff != 0 ? dbmDiff : 1;   // ensure that same dbm doesn't mean same object
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(securityLabel);
        parcel.writeString(lastSecurityLabel);
        parcel.writeString(ssid);
        parcel.writeString(bssid);
        parcel.writeInt(wifiSecurityImageResource);
        parcel.writeInt(channel);
        parcel.writeInt(frequency);
        parcel.writeInt(dbm);
        parcel.writeInt(visibilityCounter.get());
        parcel.writeLong(timestamp);
    }

    public String getSecurityLabel() {
        return securityLabel;
    }

    public String getLastSecurityLabel() {
        return lastSecurityLabel;
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
        return SystemClock.elapsedRealtime() - this.timestamp;
    }

    private String getSecurityFromCapabilities(final String capabilities) {

        if(capabilities == null) return "?";

        if(capabilities.contains("WPA2-") && capabilities.contains("WPA-")) {
            return "WPA2/WPA";
        } else if(capabilities.contains("WPA2-")) {
            return "WPA2";
        } else if(capabilities.contains("WPA-")) {
            return "WPA";
        } else if(capabilities.contains("WEP-")) {
            return "WEP";
        } else if(capabilities.contains("IBSS-")) {
            return "IBSS";
        } else return "-";

        /*
        if(capabilities != null) {
            final Matcher matcher = WIFI_SECURITY_PATTERN.matcher(capabilities);

            final StringBuilder securityLabel = new StringBuilder();
            while(matcher.find()) {
                securityLabel.append(matcher.group(0));
                securityLabel.append(" ");
            }

            if(securityLabel.length() > 0) {

                final String labelsObtained = securityLabel.toString();

                if(labelsObtained.equals("WPA2/WPA") || labelsObtained.equals("WPA/WPA2")) {
                    return "WPA2/WPA";
                } else return labelsObtained.trim();

            } else {
                return "-";
            }
        } else {
            return "?";
        }
        */
    }

    private int getImageResourceFromLabel(final String securityLabel) {
        if(securityLabel == null) {
            return R.drawable.wifi_security_unknown;
        }
        switch (securityLabel) {
            case "IBSS":
                return R.drawable.wifi_security_adhoc;
            case "-":
                return R.drawable.wifi_security_open;
            case "WPA2/WPA":
            case "WPA2":
            case "WPA":
            case "WEP":
                return R.drawable.wifi_security_wep_wpa;
            default:
                return R.drawable.wifi_security_unknown;
        }
    }
}
