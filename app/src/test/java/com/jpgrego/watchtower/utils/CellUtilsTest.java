package com.jpgrego.watchtower.utils;

import android.telephony.TelephonyManager;

import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import junitparams.naming.TestCaseName;


/**
 * Created by jpgrego on 04-09-2016.
 */
@RunWith(JUnitParamsRunner.class)
public class CellUtilsTest {

    @SuppressWarnings("unused")
    private Object[] parametersForGetGenerationFromNetworkType() {
        return new Object[][] {
                {TelephonyManager.NETWORK_TYPE_CDMA, "2G"},
                {TelephonyManager.NETWORK_TYPE_EDGE, "2G"},
                {TelephonyManager.NETWORK_TYPE_GPRS, "2G"},
                {TelephonyManager.NETWORK_TYPE_IDEN, "2G"},

                {TelephonyManager.NETWORK_TYPE_1xRTT, "3G"},
                {TelephonyManager.NETWORK_TYPE_EHRPD, "3G"},
                {TelephonyManager.NETWORK_TYPE_EVDO_0, "3G"},
                {TelephonyManager.NETWORK_TYPE_EVDO_A, "3G"},
                {TelephonyManager.NETWORK_TYPE_EVDO_B, "3G"},
                {TelephonyManager.NETWORK_TYPE_HSDPA, "3G"},
                {TelephonyManager.NETWORK_TYPE_HSPA, "3G"},
                {TelephonyManager.NETWORK_TYPE_HSPAP, "3G"},
                {TelephonyManager.NETWORK_TYPE_HSUPA, "3G"},
                {TelephonyManager.NETWORK_TYPE_UMTS, "3G"},

                {TelephonyManager.NETWORK_TYPE_LTE, "4G"}
        };
    }

    @SuppressWarnings("unused")
    private Object[] parametersForRssiToDbm() {
        return new Object[][] {
                {TelephonyManager.NETWORK_TYPE_UMTS, -6, -1111},
                {TelephonyManager.NETWORK_TYPE_UMTS, -5, -121},
                {TelephonyManager.NETWORK_TYPE_UMTS, 91, -25},
                {TelephonyManager.NETWORK_TYPE_UMTS, 92, -1111},
                {TelephonyManager.NETWORK_TYPE_UMTS, -122, -1111},
                {TelephonyManager.NETWORK_TYPE_UMTS, -121, -121},
                {TelephonyManager.NETWORK_TYPE_UMTS, -25, -25},
                {TelephonyManager.NETWORK_TYPE_UMTS, -24, -1111},

                {TelephonyManager.NETWORK_TYPE_HSDPA, -6, -1111},
                {TelephonyManager.NETWORK_TYPE_HSDPA, -5, -121},
                {TelephonyManager.NETWORK_TYPE_HSDPA, 91, -25},
                {TelephonyManager.NETWORK_TYPE_HSDPA, 92, -1111},
                {TelephonyManager.NETWORK_TYPE_HSDPA, -122, -1111},
                {TelephonyManager.NETWORK_TYPE_HSDPA, -121, -121},
                {TelephonyManager.NETWORK_TYPE_HSDPA, -25, -25},
                {TelephonyManager.NETWORK_TYPE_HSDPA, -24, -1111},

                {TelephonyManager.NETWORK_TYPE_HSUPA, -6, -1111},
                {TelephonyManager.NETWORK_TYPE_HSUPA, -5, -121},
                {TelephonyManager.NETWORK_TYPE_HSUPA, 91, -25},
                {TelephonyManager.NETWORK_TYPE_HSUPA, 92, -1111},
                {TelephonyManager.NETWORK_TYPE_HSUPA, -122, -1111},
                {TelephonyManager.NETWORK_TYPE_HSUPA, -121, -121},
                {TelephonyManager.NETWORK_TYPE_HSUPA, -25, -25},
                {TelephonyManager.NETWORK_TYPE_HSUPA, -24, -1111},

                {TelephonyManager.NETWORK_TYPE_HSPA, -6, -1111},
                {TelephonyManager.NETWORK_TYPE_HSPA, -5, -121},
                {TelephonyManager.NETWORK_TYPE_HSPA, 91, -25},
                {TelephonyManager.NETWORK_TYPE_HSPA, 92, -1111},
                {TelephonyManager.NETWORK_TYPE_HSPA, -122, -1111},
                {TelephonyManager.NETWORK_TYPE_HSPA, -121, -121},
                {TelephonyManager.NETWORK_TYPE_HSPA, -25, -25},
                {TelephonyManager.NETWORK_TYPE_HSPA, -24, -1111},

                {TelephonyManager.NETWORK_TYPE_EDGE, -1, -1111},
                {TelephonyManager.NETWORK_TYPE_EDGE, 0, -113},
                {TelephonyManager.NETWORK_TYPE_EDGE, 1, -111},
                {TelephonyManager.NETWORK_TYPE_EDGE, 31, -51},
                {TelephonyManager.NETWORK_TYPE_EDGE, 32, -1111}
        };
    }

    @Test
    @Parameters
    @TestCaseName("getGenerationFromNetworkType({0}) = {1}")
    public void getGenerationFromNetworkType(int networkType, String expResult) {
        Assert.assertEquals(expResult, CellUtils.getGenerationFromNetworkType(networkType));
    }

    @Test
    @Parameters
    @TestCaseName("rssiToDbm({0}, {1}) = {2}")
    public void rssiToDbm(int networkType, int rssi, int expResult) {
        Assert.assertEquals(expResult, CellUtils.rssiToDbm(networkType, rssi));
    }

}
