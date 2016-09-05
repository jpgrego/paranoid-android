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
 * Created by jpgrego on 04-09-2016.
 */
@RunWith(value = Parameterized.class)
public class CellUtilsTest_getGenerationFromNetworkType {

    @Parameter(value = 0)
    public int networkType;

    @Parameter(value = 1)
    public String expResult;

    @Parameters(name = "{index}: getGenerationFromNetworkType({0}) = {1}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
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
        });
    }

    @Test
    public void getGenerationFromNetworkType() {
        Assert.assertEquals(CellUtils.getGenerationFromNetworkType(networkType), expResult);
    }

}
