package com.jpgrego.thesisapp.thesisapp.fragments;

import android.telephony.CellIdentityGsm;
import android.telephony.CellInfoGsm;
import android.telephony.CellSignalStrengthGsm;
import android.widget.TableLayout;
import com.jpgrego.thesisapp.thesisapp.BuildConfig;
import com.jpgrego.thesisapp.thesisapp.activities.MainActivity;
import com.jpgrego.thesisapp.thesisapp.data.Cell;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
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
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import static com.jpgrego.thesisapp.thesisapp.TestValues.*;

/**
 * Created by jpgrego on 15-09-2016.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 19)
public class WifiAndCellFragmentTest {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    private WifiAndCellFragment wifiAndCellFragmentTest;
    private static Method updateCellTableMethod, addCellsToTableMethod, updateWifiTableMethod;
    private static Field wifiTableField, cellsTableField;

    @Mock
    private CellInfoGsm cellInfoGsmMock;

    @Mock
    private CellIdentityGsm cellIdentityGsmMock;

    @Mock
    private CellSignalStrengthGsm cellSignalStrengthGsm;

    @BeforeClass
    public static void setUpClass() throws NoSuchFieldException, NoSuchMethodException {
        updateCellTableMethod = WifiAndCellFragment.class.getDeclaredMethod("updateCellTable");
        addCellsToTableMethod = WifiAndCellFragment.class.getDeclaredMethod("addCellsToTable",
                List.class);
        updateWifiTableMethod = WifiAndCellFragment.class.getDeclaredMethod("updateWifiTable");
        wifiTableField = WifiAndCellFragment.class.getDeclaredField("wifiTable");
        cellsTableField = WifiAndCellFragment.class.getDeclaredField("cellsTable");

        wifiTableField.setAccessible(true);
        cellsTableField.setAccessible(true);
        updateCellTableMethod.setAccessible(true);
        addCellsToTableMethod.setAccessible(true);
        updateWifiTableMethod.setAccessible(true);
    }

    // this initialization is expensive, but @BeforeClass couldn't be used
    @Before
    public void setUp() {
        Mockito.when(cellInfoGsmMock.getCellIdentity()).thenReturn(cellIdentityGsmMock);
        Mockito.when(cellInfoGsmMock.getCellSignalStrength()).thenReturn(cellSignalStrengthGsm);

        wifiAndCellFragmentTest = new WifiAndCellFragment();

        SupportFragmentTestUtil.startFragment(wifiAndCellFragmentTest, MainActivity.class);
    }

    @Test
    public void mainActivityNotNull() throws NoSuchFieldException, IllegalAccessException {
        final Field mainActivityField;
        final MainActivity mainActivity;

        mainActivityField = WifiAndCellFragment.class.getDeclaredField("mainActivity");
        mainActivityField.setAccessible(true);
        mainActivity = (MainActivity) mainActivityField.get(wifiAndCellFragmentTest);

        Assert.assertNotNull(mainActivity);
    }

    @Test
    public void cellsTableNotNull() throws IllegalAccessException {
        final TableLayout cellsTable;

        cellsTable = (TableLayout) cellsTableField.get(wifiAndCellFragmentTest);

        Assert.assertNotNull(cellsTable);
    }

    @Test
    public void wifiTableNotNull() throws IllegalAccessException {
        final TableLayout wifiTable;

        wifiTable = (TableLayout) wifiTableField.get(wifiAndCellFragmentTest);

        Assert.assertNotNull(wifiTable);
    }

    @Test
    public void addCellsToTable() throws InvocationTargetException, IllegalAccessException {
        final List<Cell> cellList;
        final TableLayout cellsTable;
        final Cell cell1, cell2;

        cell1 = new Cell(TEST_NETWORK_TYPE, cellInfoGsmMock);
        cell2 = new Cell(TEST_NETWORK_TYPE, cellInfoGsmMock);
        cellList = new ArrayList<>();

        cellList.add(cell1);
        cellList.add(cell2);
        addCellsToTableMethod.invoke(wifiAndCellFragmentTest, cellList);

        cellsTable = (TableLayout) cellsTableField.get(wifiAndCellFragmentTest);

        Assert.assertTrue(cellsTable.getChildCount() == 3);
    }

    @Test
    public void updateCellTableTest() throws InvocationTargetException, IllegalAccessException {
        final TableLayout cellsTable;
        updateCellTableMethod.invoke(wifiAndCellFragmentTest);

        cellsTable = (TableLayout) cellsTableField.get(wifiAndCellFragmentTest);

        // all the rows were removed, and only the title row was added
        Assert.assertTrue(cellsTable.getChildCount() == 1);
    }

    @Test
    public void updateWifiTable() throws InvocationTargetException, IllegalAccessException {
        final TableLayout wifiTable;

        wifiTable = (TableLayout) wifiTableField.get(wifiAndCellFragmentTest);

        updateWifiTableMethod.invoke(wifiAndCellFragmentTest);

        // all the rows were removed, and only the title row was added
        Assert.assertTrue(wifiTable.getChildCount() == 1);
    }

}
