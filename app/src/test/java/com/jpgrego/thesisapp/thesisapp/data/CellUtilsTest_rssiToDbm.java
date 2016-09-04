package com.jpgrego.thesisapp.thesisapp.data;

import android.telephony.TelephonyManager;
import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import java.util.Arrays;
import java.util.Collection;


/**
 * Created by jpgrego on 03-09-2016.
 */
@RunWith(value = Parameterized.class)
public class CellUtilsTest_rssiToDbm {

    @Parameter(value = 0)
    public int networkType;

    @Parameter(value = 1)
    public int rssi;

    @Parameter(value = 2)
    public int retVal;

    @Parameters(name = "{index}: rssiToDbm({0},{1}) = {2}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
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
                {TelephonyManager.NETWORK_TYPE_EDGE, 32, -1111},
        });
    }

    @Test
    public void rssiToDbm() {
        final int result;
        result = CellUtils.rssiToDbm(networkType, rssi);
        Assert.assertEquals(retVal, result);
    }

}
