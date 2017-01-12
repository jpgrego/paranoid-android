package com.jpgrego.thesisapp.thesisapp.data;

import android.telephony.CellIdentityGsm;
import android.telephony.CellInfoGsm;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.NeighboringCellInfo;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import static com.jpgrego.thesisapp.thesisapp.TestValues.*;

/**
 * Created by jgrego on 06-09-2016.
 */
@RunWith(MockitoJUnitRunner.class)
public class CellTest {

    @Mock
    private CellInfoGsm cellInfoGsm;

    @Mock
    private CellIdentityGsm cellIdentityGsm;

    @Mock
    private CellSignalStrengthGsm cellSignalStrengthGsm;

    @Mock
    private GsmCellLocation gsmCellLocation;

    @Mock
    private NeighboringCellInfo neighboringCellInfo;

    @Before
    public void setUp() {
        Mockito.when(cellInfoGsm.getCellIdentity()).thenReturn(cellIdentityGsm);
        Mockito.when(cellInfoGsm.getCellSignalStrength()).thenReturn(cellSignalStrengthGsm);
        Mockito.when(cellIdentityGsm.getMcc()).thenReturn(TEST_MCC);
        Mockito.when(cellIdentityGsm.getMnc()).thenReturn(TEST_MNC);
        Mockito.when(cellIdentityGsm.getCid()).thenReturn(TEST_CID);
        Mockito.when(cellIdentityGsm.getLac()).thenReturn(TEST_LAC);
        //noinspection deprecation
        Mockito.when(cellIdentityGsm.getPsc()).thenReturn(TEST_PSC);
        Mockito.when(cellSignalStrengthGsm.getDbm()).thenReturn(TEST_DBM);
        Mockito.when(gsmCellLocation.getCid()).thenReturn(TEST_CID);
        Mockito.when(gsmCellLocation.getLac()).thenReturn(TEST_LAC);
        Mockito.when(gsmCellLocation.getPsc()).thenReturn(TEST_PSC);
        Mockito.when(neighboringCellInfo.getCid()).thenReturn(TEST_CID);
        Mockito.when(neighboringCellInfo.getLac()).thenReturn(TEST_LAC);
        Mockito.when(neighboringCellInfo.getPsc()).thenReturn(TEST_PSC);
        Mockito.when(neighboringCellInfo.getRssi()).thenReturn(TEST_DBM);
        Mockito.when(neighboringCellInfo.getNetworkType()).thenReturn(
                TelephonyManager.NETWORK_TYPE_UMTS);

    }

    @Test
    public void testFromCellInfoGsm1() {
        final Cell cell = Cell.fromCellInfoGsm(TEST_NETWORK_TYPE, cellInfoGsm);

        final int[] array1 = {TEST_MCC, TEST_MNC, TEST_CID, TEST_LAC, TEST_PSC, TEST_DBM};
        final int[] array2 = {cell.getMcc(), cell.getMnc(), cell.getCid(), cell.getLac(),
                cell.getPsc(), cell.getDbm()};
        Assert.assertArrayEquals(array1, array2);
    }

    @Test
    public void testFromCellInfoGsm2() {
        final Cell cell = Cell.fromCellInfoGsm(TEST_NETWORK_TYPE, cellInfoGsm);
        Assert.assertEquals("3G", cell.getGeneration());
    }

    @Test
    public void testFromGsmCellLocation1() {
        final Cell cell = Cell.fromGsmCellLocation(TEST_NETWORK_TYPE, TEST_MCC, TEST_MNC, TEST_DBM,
                gsmCellLocation);
        final int[] array1 = {TEST_MCC, TEST_MNC, TEST_CID, TEST_LAC, TEST_PSC, TEST_DBM};
        final int[] array2 = {cell.getMcc(), cell.getMnc(), cell.getCid(), cell.getLac(),
                cell.getPsc(), cell.getDbm()};
        Assert.assertArrayEquals(array1, array2);
    }

    @Test
    public void testFromGsmCellLocation2() {
        final Cell cell = Cell.fromGsmCellLocation(TEST_NETWORK_TYPE, TEST_MCC, TEST_MNC, TEST_DBM,
                gsmCellLocation);
        Assert.assertEquals("3G", cell.getGeneration());
    }

    @Test
    public void testFromNeighboringCellInfo1() {
        final Cell cell = Cell.fromNeighboringCellInfo(TEST_MCC, TEST_MNC, neighboringCellInfo);

        final int[] array1 = {TEST_MCC, TEST_MNC, TEST_CID, TEST_LAC, TEST_PSC, TEST_DBM};
        final int[] array2 = {cell.getMcc(), cell.getMnc(), cell.getCid(), cell.getLac(),
                cell.getPsc(), cell.getDbm()};
        Assert.assertArrayEquals(array1, array2);
    }

    @Test
    public void testFromNeighboringCellInfo2() {
        final Cell cell = Cell.fromNeighboringCellInfo(TEST_MCC, TEST_MNC, neighboringCellInfo);
        Assert.assertEquals("3G", cell.getGeneration());
    }
}
