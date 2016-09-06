package com.jpgrego.thesisapp.thesisapp.data;

import android.telephony.CellIdentityGsm;
import android.telephony.CellInfoGsm;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.NeighboringCellInfo;
import android.telephony.gsm.GsmCellLocation;

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
        Mockito.when(cellIdentityGsm.getPsc()).thenReturn(TEST_PSC);
        Mockito.when(cellSignalStrengthGsm.getDbm()).thenReturn(TEST_DBM);
        Mockito.when(gsmCellLocation.getCid()).thenReturn(TEST_CID);
        Mockito.when(gsmCellLocation.getLac()).thenReturn(TEST_LAC);
        Mockito.when(gsmCellLocation.getPsc()).thenReturn(TEST_PSC);
        Mockito.when(neighboringCellInfo.getCid()).thenReturn(TEST_CID);
        Mockito.when(neighboringCellInfo.getLac()).thenReturn(TEST_LAC);
        Mockito.when(neighboringCellInfo.getPsc()).thenReturn(TEST_PSC);

        cell1 = new Cell(TEST_NETWORK_TYPE, cellInfoGsm);
        cell2 = new Cell(TEST_NETWORK_TYPE, TEST_MCC, TEST_MNC, gsmCellLocation);
        cell3 = new Cell(TEST_NETWORK_TYPE, TEST_MCC, TEST_MNC, neighboringCellInfo);
    }

    @Test
    public void toImplement() {

    }
}
