package com.jpgrego.thesisapp.thesisapp.utils;

import android.telephony.TelephonyManager;

/**
 * Created by jpgrego on 03-09-2016.
 */
public final class CellUtils {

    // enforce noninstantiability
    private CellUtils() {
        throw new AssertionError();
    }

    /*
     * thanks SatStat!
     */
    public static int rssiToDbm(int networkType, int rssi) {
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
                if ((rssi >= 0) && (rssi <= 31)) {
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
    public static String getGenerationFromNetworkType(int networkType) {
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
}
