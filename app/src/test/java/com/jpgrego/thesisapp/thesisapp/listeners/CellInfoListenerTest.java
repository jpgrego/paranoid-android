package com.jpgrego.thesisapp.thesisapp.listeners;

import android.telephony.CellIdentityGsm;
import android.telephony.CellInfo;
import android.telephony.CellInfoGsm;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.NeighboringCellInfo;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import com.jpgrego.thesisapp.thesisapp.data.Cell;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import static com.jpgrego.thesisapp.thesisapp.TestValues.*;

/**
 * Created by jgrego on 08-09-2016.
 */
@RunWith(MockitoJUnitRunner.class)
public class CellInfoListenerTest {

    private CellInfoListener cellInfoListenerTest;
    private static Method getAllCellsMethod, getRegisteredCellInfoMethod,
            getNeighboringCellsInfoMethod;
    private static Field cellListField, mccField, mncField, telephonyManagerField,
            registeredCellSignalStrengthField;

    @Mock
    private TelephonyManager telephonyManagerMock;

    @Mock
    private CellInfoGsm cellInfoGsmMock1, cellInfoGsmMock2;

    @Mock
    private CellIdentityGsm cellIdentityGsmMock;

    @Mock
    private CellSignalStrengthGsm cellSignalStrengthMock1, cellSignalStrengthMock2;

    @Mock
    private GsmCellLocation gsmCellLocationMock;

    @Mock
    private NeighboringCellInfo neighboringCellInfoMock;

    @Mock
    private SignalStrength signalStrengthMock;

    @BeforeClass
    public static void setUpClass() throws NoSuchFieldException, NoSuchMethodException {
        getAllCellsMethod = CellInfoListener.class.getDeclaredMethod("getAllCellInfo");
        getRegisteredCellInfoMethod = CellInfoListener.class.getDeclaredMethod(
                "getRegisteredCellInfo");
        getNeighboringCellsInfoMethod = CellInfoListener.class.getDeclaredMethod(
                "getNeighboringCellsInfo");
        cellListField = CellInfoListener.class.getDeclaredField("cellList");
        mccField = CellInfoListener.class.getDeclaredField("mcc");
        mncField = CellInfoListener.class.getDeclaredField("mnc");
        telephonyManagerField = CellInfoListener.class.getDeclaredField("telephonyManager");
        registeredCellSignalStrengthField = CellInfoListener.class.getDeclaredField(
                "registedCellSignalStrength");

        getAllCellsMethod.setAccessible(true);
        getRegisteredCellInfoMethod.setAccessible(true);
        getNeighboringCellsInfoMethod.setAccessible(true);
        cellListField.setAccessible(true);
        mccField.setAccessible(true);
        mncField.setAccessible(true);
        telephonyManagerField.setAccessible(true);
        registeredCellSignalStrengthField.setAccessible(true);
    }

    @Before
    public void setUp() {
        Mockito.when(telephonyManagerMock.getNetworkOperator()).thenReturn(TEST_NETWORK_OPERATOR);
        Mockito.when(telephonyManagerMock.getNetworkType()).thenReturn(TEST_NETWORK_TYPE);

        cellInfoListenerTest = new CellInfoListener(telephonyManagerMock);
    }

    @Test
    public void cellInfoListenerConstructor() throws IllegalAccessException {
        final TelephonyManager telephonyManager;

        telephonyManager = (TelephonyManager) telephonyManagerField.get(cellInfoListenerTest);

        Assert.assertEquals(telephonyManagerMock, telephonyManager);
    }

    @Test
    public void getAllCellInfoReturnsFalseOnNull() throws InvocationTargetException,
            IllegalAccessException {
        Mockito.when(telephonyManagerMock.getAllCellInfo()).thenReturn(null);

        final boolean retVal;

        retVal = (boolean) getAllCellsMethod.invoke(cellInfoListenerTest);
        Assert.assertFalse(retVal);
    }

    @Test
    public void getAllCellInfoReturnsTrueOnList() throws InvocationTargetException,
            IllegalAccessException {
        Mockito.when(telephonyManagerMock.getAllCellInfo()).thenReturn(new ArrayList<CellInfo>());

        final boolean retVal;

        retVal = (boolean) getAllCellsMethod.invoke(cellInfoListenerTest);
        Assert.assertTrue(retVal);
    }

    @Test
    public void getAllCellInfoAddsToList() throws IllegalAccessException,
            InvocationTargetException {
        final ArrayList<CellInfo> inputList = new ArrayList<>();
        final ArrayList retVal;

        cellListField.set(cellInfoListenerTest, new ArrayList<>());
        inputList.add(cellInfoGsmMock1);

        Mockito.when(telephonyManagerMock.getAllCellInfo()).thenReturn(inputList);
        Mockito.when(cellInfoGsmMock1.getCellIdentity()).thenReturn(cellIdentityGsmMock);
        Mockito.when(cellInfoGsmMock1.getCellSignalStrength()).thenReturn(cellSignalStrengthMock1);

        getAllCellsMethod.invoke(cellInfoListenerTest);
        retVal = (ArrayList) cellListField.get(cellInfoListenerTest);

        Assert.assertTrue(retVal.size() == 1);
    }

    @Test
    public void getRegisteredCellInfoAddsToList() throws IllegalAccessException,
            InvocationTargetException {
        final ArrayList retVal;

        Mockito.when(telephonyManagerMock.getCellLocation()).thenReturn(gsmCellLocationMock);

        getRegisteredCellInfoMethod.invoke(cellInfoListenerTest);
        retVal = (ArrayList) cellListField.get(cellInfoListenerTest);

        Assert.assertTrue(retVal.size() == 1);
    }

    @Test
    public void getNeighboringCellsInfoAddsToList() throws IllegalAccessException,
            InvocationTargetException {
        final ArrayList<NeighboringCellInfo> inputList = new ArrayList<>();
        final ArrayList retVal;

        cellListField.set(cellInfoListenerTest, new ArrayList<>());
        inputList.add(neighboringCellInfoMock);

        //noinspection deprecation
        Mockito.when(telephonyManagerMock.getNeighboringCellInfo()).thenReturn(inputList);

        getNeighboringCellsInfoMethod.invoke(cellInfoListenerTest);
        retVal = (ArrayList) cellListField.get(cellInfoListenerTest);

        Assert.assertTrue(retVal.size() == 1);
    }

    @Test
    public void getSortedCellList() throws IllegalAccessException {
        final ArrayList<Cell> inputList;
        final List<Cell> retVal;
        final Cell testCell1, testCell2;

        Mockito.when(cellInfoGsmMock1.getCellSignalStrength()).thenReturn(cellSignalStrengthMock1);
        Mockito.when(cellInfoGsmMock2.getCellSignalStrength()).thenReturn(cellSignalStrengthMock2);
        Mockito.when(cellInfoGsmMock1.getCellIdentity()).thenReturn(cellIdentityGsmMock);
        Mockito.when(cellInfoGsmMock2.getCellIdentity()).thenReturn(cellIdentityGsmMock);
        Mockito.when(cellSignalStrengthMock1.getDbm()).thenReturn(-40);
        Mockito.when(cellSignalStrengthMock2.getDbm()).thenReturn(-30);

        inputList = new ArrayList<>();
        testCell1 = Cell.fromCellInfoGsm(TEST_NETWORK_TYPE, cellInfoGsmMock1);
        testCell2 = Cell.fromCellInfoGsm(TEST_NETWORK_TYPE, cellInfoGsmMock2);

        inputList.add(testCell1);
        inputList.add(testCell2);
        cellListField.set(cellInfoListenerTest, inputList);

        retVal = cellInfoListenerTest.getSortedCellList();

        Assert.assertTrue(retVal.size() == 2 && retVal.get(0).getDbm() == -30);
    }

    @Test
    public void onSignalStrengthsChangedTestMccAssignment() throws IllegalAccessException {
        final int mccVal;

        cellInfoListenerTest.onSignalStrengthsChanged(signalStrengthMock);
        mccVal = mccField.getInt(cellInfoListenerTest);
        Assert.assertEquals(TEST_MCC, mccVal);
    }

    @Test
    public void onSignalStrengthsChangedTestMncAssignment() throws IllegalAccessException {
        final int mncVal;

        cellInfoListenerTest.onSignalStrengthsChanged(signalStrengthMock);
        mncVal = mncField.getInt(cellInfoListenerTest);
        Assert.assertEquals(TEST_MNC, mncVal);
    }

    @Test
    public void onSignalStrengthsChangedTestSignalStrength() throws IllegalAccessException {
        final int registeredCellSignalStrength;

        Mockito.when(signalStrengthMock.getGsmSignalStrength()).thenReturn(TEST_SIGNAL_STRENGTH);
        cellInfoListenerTest.onSignalStrengthsChanged(signalStrengthMock);
        registeredCellSignalStrength = registeredCellSignalStrengthField.
                getInt(cellInfoListenerTest);

        Assert.assertEquals(TEST_DBM, registeredCellSignalStrength);
    }
}
