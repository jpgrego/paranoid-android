package com.jpgrego.thesisapp.thesisapp.fragments;

import android.net.wifi.ScanResult;
import android.telephony.CellIdentityGsm;
import android.telephony.CellInfoGsm;
import android.telephony.CellSignalStrengthGsm;
import com.jpgrego.thesisapp.thesisapp.BuildConfig;
import com.jpgrego.thesisapp.thesisapp.activities.MainActivity;
import com.jpgrego.thesisapp.thesisapp.data.Cell;
import com.jpgrego.thesisapp.thesisapp.data.WifiAP;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.support.v4.SupportFragmentTestUtil;
import java.util.ArrayList;
import java.util.List;
import static com.jpgrego.thesisapp.thesisapp.TestValues.*;

/**
 * Created by jpgrego on 15-09-2016.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 19)
public class WifiAndCellFragmentIT {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    private WifiAndCellFragment wifiAndCellFragmentTest;

    @Mock
    private CellInfoGsm cellInfoGsmMock;

    @Mock
    private CellIdentityGsm cellIdentityGsmMock;

    @Mock
    private ScanResult wifiScanResultMock;

    @Mock
    private CellSignalStrengthGsm cellSignalStrengthGsm;

    // this initialization is expensive, but @BeforeClass couldn't be used
    @Before
    public void setUp() {
        Mockito.when(cellInfoGsmMock.getCellIdentity()).thenReturn(cellIdentityGsmMock);
        Mockito.when(cellInfoGsmMock.getCellSignalStrength()).thenReturn(cellSignalStrengthGsm);
        wifiScanResultMock.BSSID = "";

        wifiAndCellFragmentTest = new WifiAndCellFragment();

        SupportFragmentTestUtil.startFragment(wifiAndCellFragmentTest, MainActivity.class);
    }

    @Test
    public void cellsTableNotNull() {
        Assert.assertNotNull(wifiAndCellFragmentTest.cellsTable);
    }

    @Test
    public void wifiTableNotNull() {
        Assert.assertNotNull(wifiAndCellFragmentTest.wifiTable);
    }

    @Test
    public void addCellsToTable() {
        final List<Cell> cellList;
        final Cell cell;

        cell = Cell.fromCellInfo(TEST_NETWORK_TYPE, cellInfoGsmMock);
        cellList = new ArrayList<>();

        cellList.add(cell);
        cellList.add(cell);

        wifiAndCellFragmentTest.cellsTable.removeAllViews();
        wifiAndCellFragmentTest.addCellsToTable(cellList);

        Assert.assertEquals(2, wifiAndCellFragmentTest.cellsTable.getChildCount());
    }

    @Test
    public void addWifiAPToTable() {
        final WifiAP wifiAP = WifiAP.fromScanResult(wifiScanResultMock);
        final List<WifiAP> wifiAPList = new ArrayList<>();
        wifiAPList.add(wifiAP);
        wifiAPList.add(wifiAP);

        wifiAndCellFragmentTest.wifiTable.removeAllViews();
        wifiAndCellFragmentTest.addWifiAPToTable(wifiAPList, "");

        Assert.assertEquals(2, wifiAndCellFragmentTest.wifiTable.getChildCount());

    }

    @Test
    public void updateCellTableTest() {
        wifiAndCellFragmentTest.cellsTable.removeAllViews();
        wifiAndCellFragmentTest.updateCellTable(new ArrayList<Cell>());

        // all the rows were removed, and only the title row was added
        Assert.assertEquals(1, wifiAndCellFragmentTest.cellsTable.getChildCount());
    }

    @Test
    public void updateWifiTable() {
        wifiAndCellFragmentTest.wifiTable.removeAllViews();
        wifiAndCellFragmentTest.updateWifiTable(new ArrayList<WifiAP>(), "");

        // all the rows were removed, and only the title row was added
        Assert.assertEquals(1, wifiAndCellFragmentTest.wifiTable.getChildCount());
    }

}
