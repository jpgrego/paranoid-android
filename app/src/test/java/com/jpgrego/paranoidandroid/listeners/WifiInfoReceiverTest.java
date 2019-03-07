package com.jpgrego.paranoidandroid.listeners;

import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;

import com.jpgrego.paranoidandroid.data.WifiAP;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by jpgrego on 13-09-2016.
 */
@RunWith(MockitoJUnitRunner.class)
public class WifiInfoReceiverTest {

    private WifiInfoReceiver wifiInfoReceiverTest;
    private static Field wifiAPSetField, wifiManagerField;

    @Mock
    private WifiManager wifiManagerMock;

    @Mock
    private ScanResult scanResultMock1, scanResultMock2;

    @Mock
    private Context contextMock;

    @Mock
    private Intent intentMock;

    @Mock
    private NetworkInfo networkInfo;

    @BeforeClass
    public static void setUpClass() throws NoSuchFieldException {
        wifiManagerField = WifiInfoReceiver.class.getDeclaredField("wifiManager");
        wifiAPSetField = WifiInfoReceiver.class.getDeclaredField("wifiAPSet");

        wifiManagerField.setAccessible(true);
        wifiAPSetField.setAccessible(true);
    }

    @Before
    public void setUp() {
        //wifiInfoReceiverTest = new WifiInfoReceiver(wifiManagerMock);
    }

    @Test
    public void wifiInfoReceiverConstructor() throws IllegalAccessException {
        final WifiManager wifiManager;

        wifiManager = (WifiManager) wifiManagerField.get(wifiInfoReceiverTest);
        Assert.assertEquals(wifiManagerMock, wifiManager);
    }

    @Test
    public void onReceiveNewConnection() {
        final String testBSSID = "00:11:22:33:44:55";

        Mockito.when(intentMock.getAction()).thenReturn(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        Mockito.when(intentMock.getStringExtra(WifiManager.EXTRA_BSSID))
                .thenReturn(testBSSID);
        Mockito.when(networkInfo.isConnected()).thenReturn(true);
        Mockito.when(intentMock.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO))
                .thenReturn(networkInfo);

        wifiInfoReceiverTest.onReceive(contextMock, intentMock);
        Assert.assertEquals(testBSSID, wifiInfoReceiverTest.getCurrentWifiConnectionBSSID());
    }

    @Test
    public void onReceiveDisconnected() {
        Mockito.when(intentMock.getAction()).thenReturn(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        Mockito.when(networkInfo.isConnected()).thenReturn(false);
        Mockito.when(intentMock.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO))
                .thenReturn(networkInfo);

        wifiInfoReceiverTest.onReceive(contextMock, intentMock);
        Assert.assertEquals("", wifiInfoReceiverTest.getCurrentWifiConnectionBSSID());
    }

    @Test
    public void onReceiveTestUniqueness() throws IllegalAccessException {
        final List<ScanResult> scanResultList;
        final Set retVal;

        scanResultMock1.level = -80;
        scanResultMock1.BSSID = "00:11:22:33:44:55";
        scanResultMock2.level = -80;
        scanResultMock2.BSSID = "00:11:22:33:44:55";
        scanResultList = new ArrayList<>();

        scanResultList.add(scanResultMock1);
        scanResultList.add(scanResultMock2);

        Mockito.when(intentMock.getAction()).thenReturn(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        Mockito.when(wifiManagerMock.getScanResults()).thenReturn(scanResultList);
        wifiInfoReceiverTest.onReceive(contextMock, intentMock);

        retVal = (Set) wifiAPSetField.get(wifiInfoReceiverTest);
        Assert.assertTrue(retVal.size() == 1);
    }

    @Test
    public void onReceiveTestNonUniqueness() throws IllegalAccessException {
        final List<ScanResult> scanResultList;
        final Set retVal;

        scanResultMock1.level = -80;
        scanResultMock1.BSSID = "00:11:22:33:44:55";
        scanResultMock2.level = -70;
        scanResultMock2.BSSID = "00:11:22:33:44:56";
        scanResultList = new ArrayList<>();

        scanResultList.add(scanResultMock1);
        scanResultList.add(scanResultMock2);

        Mockito.when(intentMock.getAction()).thenReturn(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        Mockito.when(wifiManagerMock.getScanResults()).thenReturn(scanResultList);
        wifiInfoReceiverTest.onReceive(contextMock, intentMock);

        retVal = (Set) wifiAPSetField.get(wifiInfoReceiverTest);
        Assert.assertTrue(retVal.size() == 2);
    }

    @Test
    public void getOrderedWifiAPList() throws IllegalAccessException {
        final List<ScanResult> scanResultList;
        final List<WifiAP> retVal;

        scanResultMock1.level = -80;
        scanResultMock1.BSSID = "00:11:22:33:44:55";
        scanResultMock2.level = -70;
        scanResultMock2.BSSID = "00:11:22:33:44:56";
        scanResultList = new ArrayList<>();

        scanResultList.add(scanResultMock1);
        scanResultList.add(scanResultMock2);

        Mockito.when(intentMock.getAction()).thenReturn(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        Mockito.when(wifiManagerMock.getScanResults()).thenReturn(scanResultList);
        wifiInfoReceiverTest.onReceive(contextMock, intentMock);

        retVal = wifiInfoReceiverTest.getOrderedWifiAPList();
        Assert.assertTrue(retVal.get(0).getDbm() == -70);
    }

}
