package com.jpgrego.thesisapp.thesisapp;

import android.telephony.TelephonyManager;

/**
 * Created by jpgrego on 12-09-2016.
 */
public class TestValues {

    // enforce noninstantiability
    private TestValues() {
        throw new AssertionError();
    }

    public static final String  TEST_NETWORK_OPERATOR   = "26801";
    public static final int     TEST_MCC                = 268;
    public static final int     TEST_MNC                = 1;
    public static final int     TEST_CID                = 215931256;
    public static final int     TEST_LAC                = 51;
    public static final int     TEST_PSC                = 420;
    public static final int     TEST_SIGNAL_STRENGTH    = 30;
    public static final int     TEST_DBM                = TEST_SIGNAL_STRENGTH * 2 - 113;
    public static final int     TEST_NETWORK_TYPE       = TelephonyManager.NETWORK_TYPE_UMTS;
}
