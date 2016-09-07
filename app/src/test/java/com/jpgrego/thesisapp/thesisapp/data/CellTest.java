package com.jpgrego.thesisapp.thesisapp.data;

import android.telephony.CellIdentityGsm;
import android.telephony.CellInfoGsm;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.NeighboringCellInfo;
import android.telephony.gsm.GsmCellLocation;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Created by jgrego on 06-09-2016.
 */
@RunWith(MockitoJUnitRunner.class)
public class CellTest {

    private static final int TEST_MCC           = 268;
    private static final int TEST_MNC           = 1;
    private static final int TEST_CID           = 215931256;
    private static final int TEST_LAC           = 51;
    private static final int TEST_PSC           = 420;
    private static final int TEST_DBM           = -80;
    private static final int TEST_NETWORK_TYPE  = 10;

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

    private Cell cell1, cell2, cell3;

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

        cell1 = new Cell(TEST_NETWORK_TYPE, cellInfoGsm);
        cell2 = new Cell(TEST_NETWORK_TYPE, TEST_MCC, TEST_MNC, gsmCellLocation);
        cell3 = new Cell(TEST_NETWORK_TYPE, TEST_MCC, TEST_MNC, neighboringCellInfo);
    }

    @Test
    public void testCellConstructor1_1() {
        final int[] array1 = {TEST_MCC, TEST_MNC, TEST_CID, TEST_LAC, TEST_PSC, TEST_DBM};
        final int[] array2 = {cell1.mcc, cell1.mnc, cell1.cid, cell1.lac, cell1.psc, cell1.dbm};
        Assert.assertArrayEquals(array1, array2);
    }

    @Test
    public void testCellConstructor1_2() {
        Assert.assertEquals("3G", cell1.generation);
    }

    @Test
    public void testCellConstructor2_1() {
        final int[] array1 = {TEST_MCC, TEST_MNC, TEST_CID, TEST_LAC, TEST_PSC, -1};
        final int[] array2 = {cell2.mcc, cell2.mnc, cell2.cid, cell2.lac, cell2.psc, cell2.dbm};
        Assert.assertArrayEquals(array1, array2);
    }

    @Test
    public void testCellConstructor2_2() {
        Assert.assertEquals("3G", cell2.generation);
    }

    @Test
    public void testCellConstructor3_1() {
        final int[] array1 = {TEST_MCC, TEST_MNC, TEST_CID, TEST_LAC, TEST_PSC, TEST_DBM};
        final int[] array2 = {cell3.mcc, cell3.mnc, cell3.cid, cell3.lac, cell3.psc, cell3.dbm};
        Assert.assertArrayEquals(array1, array2);
    }

    @Test
    public void testCellConstructor3_2() {
        Assert.assertEquals("3G", cell3.generation);
    }

    @Test
    public void testSetRegisteredCell() {
        final Cell testCell = new Cell(TEST_NETWORK_TYPE, cellInfoGsm);
        testCell.setRegisteredCell();
        Assert.assertTrue(testCell.isRegisteredCell());
    }
}
