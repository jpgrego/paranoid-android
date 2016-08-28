package com.jpgrego.thesisapp.thesisapp.data;

import android.support.annotation.NonNull;
import android.telephony.NeighboringCellInfo;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;

/**
 * Created by jpgrego on 25-08-2016.
 */
public class Cell implements Comparable<Cell> {
    public final int cid, lac, psc, dbm;
    public final String generation;
    private boolean isRegisteredCell = false;

    public Cell(final int networkType, @NonNull final GsmCellLocation cellLocation) {
        this.cid = cellLocation.getCid();
        this.lac = cellLocation.getLac();
        this.psc = cellLocation.getPsc();
        this.dbm = -1;
        this.generation = getGenerationFromNetworkType(networkType);
    }

    public Cell(final int networkType, @NonNull final NeighboringCellInfo neighboringCellInfo) {
        this.cid = neighboringCellInfo.getCid();
        this.lac = neighboringCellInfo.getLac();
        this.psc = neighboringCellInfo.getPsc();
        this.dbm = rssiToDbm(networkType, neighboringCellInfo.getRssi());
        this.generation = getGenerationFromNetworkType(networkType);
    }

    /*
    * thanks SatStat!
    */
    private int rssiToDbm(int networkType, int rssi) {
        switch (networkType) {
            case TelephonyManager.NETWORK_TYPE_UMTS:
            case TelephonyManager.NETWORK_TYPE_HSDPA:
            case TelephonyManager.NETWORK_TYPE_HSUPA:
            case TelephonyManager.NETWORK_TYPE_HSPA:
                /*
                 * for details see TS 25.133 section 9.1.1.3
				 * http://www.3gpp.org/DynaReport/25133.htm
				 */
                if ((rssi >= -5) && (rssi <= 91)) {
                    return rssi - 116;
                } else if ((rssi >= -121) && (rssi <= -25)) {
                    return rssi;
                }
                return -1111;
            case TelephonyManager.NETWORK_TYPE_EDGE:
            case TelephonyManager.NETWORK_TYPE_GPRS:
                if ((rssi >= 0) || (rssi <= 31)) {
                    return rssi * 2 - 113;
                }
                return -1111;
            default:
                return -1111;
        }
    }

    /*
     * Thanks SatStat!
     */
    private String getGenerationFromNetworkType(int networkType) {
        switch (networkType) {
            case TelephonyManager.NETWORK_TYPE_CDMA:
            case TelephonyManager.NETWORK_TYPE_EDGE:
            case TelephonyManager.NETWORK_TYPE_GPRS:
            case TelephonyManager.NETWORK_TYPE_IDEN:
                return "2G";
            case TelephonyManager.NETWORK_TYPE_1xRTT:
            case TelephonyManager.NETWORK_TYPE_EHRPD:
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
            case TelephonyManager.NETWORK_TYPE_HSDPA:
            case TelephonyManager.NETWORK_TYPE_HSPA:
            case TelephonyManager.NETWORK_TYPE_HSPAP:
            case TelephonyManager.NETWORK_TYPE_HSUPA:
            case TelephonyManager.NETWORK_TYPE_UMTS:
                return "3G";
            case TelephonyManager.NETWORK_TYPE_LTE:
                return "4G";
            default:
                return "?";
        }
    }

    public void setRegisteredCell() {
        isRegisteredCell = true;
    }

    public boolean isRegisteredCell() {
        return isRegisteredCell;
    }

    @Override
    public int compareTo(@NonNull Cell another) {
        return another.dbm - this.dbm;
    }
}
