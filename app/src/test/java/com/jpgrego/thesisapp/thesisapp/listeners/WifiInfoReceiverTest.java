package com.jpgrego.thesisapp.thesisapp.listeners;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;

import com.jpgrego.thesisapp.thesisapp.data.WifiAP;

import junit.framework.Assert;

import org.junit.Before;
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
    private Field wifiAPSetField, wifiManagerField;

    @Mock
    private WifiManager wifiManagerMock;

    @Mock
    private ScanResult scanResultMock1, scanResultMock2;

    @Mock
    private Context contextMock;

    @Mock
    private Intent intentMock;

    @Before
    public void setUp() throws NoSuchFieldException {
        wifiInfoReceiverTest = new WifiInfoReceiver(wifiManagerMock);
        wifiManagerField = WifiInfoReceiver.class.getDeclaredField("wifiManager");
        wifiAPSetField = WifiInfoReceiver.class.getDeclaredField("wifiAPSet");

        wifiManagerField.setAccessible(true);
        wifiAPSetField.setAccessible(true);
    }

    @Test
    public void wifiInfoReceiverConstructor() throws IllegalAccessException {
        final WifiManager wifiManager;

        wifiManager = (WifiManager) wifiManagerField.get(wifiInfoReceiverTest);
        Assert.assertEquals(wifiManagerMock, wifiManager);
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

        Mockito.when(wifiManagerMock.getScanResults()).thenReturn(scanResultList);
        wifiInfoReceiverTest.onReceive(contextMock, intentMock);

        retVal = wifiInfoReceiverTest.getOrderedWifiAPList();
        Assert.assertTrue(retVal.get(0).dbm == -70);
    }
}
