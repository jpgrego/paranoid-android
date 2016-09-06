package com.jpgrego.thesisapp.thesisapp.data;

import android.net.wifi.ScanResult;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Created by jgrego on 06-09-2016.
 */
@RunWith(MockitoJUnitRunner.class)
public class WifiAPTest {

    @Mock
    private ScanResult scanResult, equalsScanResultTest, compareScanResultTest;

    private WifiAP testAP;

    @Before
    public void setUp() {
        // testing capabilities would involve external dependencies
        scanResult.capabilities = "";
        scanResult.SSID = "TEST AP";
        scanResult.BSSID = "00:11:22:33:44:55";
        scanResult.frequency = 2412;
        scanResult.level = -40;
        testAP = new WifiAP(scanResult);

        equalsScanResultTest.capabilities = "";
        equalsScanResultTest.SSID = "TEST AP";
        equalsScanResultTest.BSSID = "00:11:22:33:44:56";
        equalsScanResultTest.frequency = 2412;
        equalsScanResultTest.level = -40;

    }

    @Test
    public void testWifiAPConstructorSSID() {
        Assert.assertEquals(scanResult.SSID, testAP.ssid);
    }

    @Test
    public void testWifiAPConstructorBSSID() {
        Assert.assertEquals(scanResult.BSSID, testAP.bssid);
    }

    @Test
    public void testWifiAPConstructorFrequency() {
        Assert.assertEquals(1, testAP.channel);
    }

    @Test
    public void testWifiAPConstructorLevel() {
        Assert.assertEquals(scanResult.level, testAP.dbm);
    }

    @Test
    public void testGetVisibilityCounter() {
        Assert.assertEquals(testAP.getVisibilityCounter(), 3);
    }

    @Test
    public void testDecrementVisibilityCounter() {
        testAP.decrementVisibilityCounter();
        Assert.assertEquals(testAP.getVisibilityCounter(), 2);
    }

    @Test
    public void testEqualsTrue() {
        final WifiAP equalsTestAP = new WifiAP(scanResult);
        Assert.assertTrue(testAP.equals(equalsTestAP));
    }

    @Test
    public void testEqualsFalse() {
        final WifiAP equalsTestAP = new WifiAP(equalsScanResultTest);
        Assert.assertFalse(testAP.equals(equalsTestAP));
    }

    @Test
    public void testHashCode() {
        Assert.assertEquals(scanResult.BSSID.hashCode(), testAP.hashCode());
    }

    @Test
    public void testCompareToGreater() {
        compareScanResultTest.level = scanResult.level - 1;
        final WifiAP compareGreaterTestAP = new WifiAP(compareScanResultTest);
        Assert.assertTrue(testAP.compareTo(compareGreaterTestAP) > 0);
    }

    @Test
    public void testCompareToLess() {
        compareScanResultTest.level = scanResult.level + 1;
        final WifiAP compareLessTestAP = new WifiAP(compareScanResultTest);
        Assert.assertTrue(testAP.compareTo(compareLessTestAP) < 0);
    }

    @Test
    public void testCompareToEquals() {
        compareScanResultTest.level = scanResult.level;
        final WifiAP compareEqualsTestAP = new WifiAP(compareScanResultTest);
        Assert.assertTrue(testAP.compareTo(compareEqualsTestAP) == 0);
    }

}
