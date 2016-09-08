package com.jpgrego.thesisapp.thesisapp.listeners;

import android.telephony.CellIdentityGsm;
import android.telephony.CellInfo;
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
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

/**
 * Created by jgrego on 08-09-2016.
 */
@RunWith(MockitoJUnitRunner.class)
public class CellInfoListenerTest {

    private static final String TEST_NETWORK_OPERATOR = "26801";

    private CellInfoListener cellInfoListenerTest;
    private Method getAllCellsMethod;
    private Method getRegisteredCellInfoMethod;
    private Method getNeighboringCellsInfoMethod;
    private Field cellListField;

    @Mock
    private TelephonyManager telephonyManager;

    @Mock
    private CellInfoGsm cellInfoGsmMock;

    @Mock
    private CellIdentityGsm cellIdentityGsmMock;

    @Mock
    private CellSignalStrengthGsm cellSignalStrengthMock;

    @Mock
    private GsmCellLocation gsmCellLocationMock;

    @Mock
    private NeighboringCellInfo neighboringCellInfoMock;

    @Before
    public void setUp() throws NoSuchMethodException, NoSuchFieldException {
        Mockito.when(telephonyManager.getNetworkOperator()).thenReturn(TEST_NETWORK_OPERATOR);

        cellInfoListenerTest = new CellInfoListener(telephonyManager);
        getAllCellsMethod = CellInfoListener.class.getDeclaredMethod("getAllCellInfo");
        getRegisteredCellInfoMethod = CellInfoListener.class.getDeclaredMethod("getRegisteredCellInfo");
        getNeighboringCellsInfoMethod = CellInfoListener.class.getDeclaredMethod("getNeighboringCellsInfo");
        cellListField = CellInfoListener.class.getDeclaredField("cellList");

        getAllCellsMethod.setAccessible(true);
        getRegisteredCellInfoMethod.setAccessible(true);
        getNeighboringCellsInfoMethod.setAccessible(true);
        cellListField.setAccessible(true);

    }

    @Test
    public void getAllCellInfoReturnsFalseOnNull() throws InvocationTargetException, IllegalAccessException {
        Mockito.when(telephonyManager.getAllCellInfo()).thenReturn(null);

        final boolean retVal;

        retVal = (boolean) getAllCellsMethod.invoke(cellInfoListenerTest);
        Assert.assertFalse(retVal);
    }

    @Test
    public void getAllCellInfoReturnsTrueOnList() throws InvocationTargetException, IllegalAccessException {
        Mockito.when(telephonyManager.getAllCellInfo()).thenReturn(new ArrayList<CellInfo>());

        final boolean retVal;

        retVal = (boolean) getAllCellsMethod.invoke(cellInfoListenerTest);
        Assert.assertTrue(retVal);
    }

    @Test
    public void getAllCellInfoAddsToList() throws IllegalAccessException, InvocationTargetException {
        final ArrayList<CellInfo> inputList = new ArrayList<>();
        final ArrayList retVal;

        cellListField.set(cellInfoListenerTest, new ArrayList<>());
        inputList.add(cellInfoGsmMock);

        Mockito.when(telephonyManager.getAllCellInfo()).thenReturn(inputList);
        Mockito.when(cellInfoGsmMock.getCellIdentity()).thenReturn(cellIdentityGsmMock);
        Mockito.when(cellInfoGsmMock.getCellSignalStrength()).thenReturn(cellSignalStrengthMock);

        getAllCellsMethod.invoke(cellInfoListenerTest);
        retVal = (ArrayList) cellListField.get(cellInfoListenerTest);

        Assert.assertTrue(retVal.size() == 1);
    }

    @Test
    public void getRegisteredCellInfoAddsToList() throws IllegalAccessException, InvocationTargetException {
        final ArrayList retVal;

        Mockito.when(telephonyManager.getCellLocation()).thenReturn(gsmCellLocationMock);

        getRegisteredCellInfoMethod.invoke(cellInfoListenerTest);
        retVal = (ArrayList) cellListField.get(cellInfoListenerTest);

        Assert.assertTrue(retVal.size() == 1);
    }

    @Test
    public void getNeighboringCellsInfoAddsToList() throws IllegalAccessException, InvocationTargetException {
        final ArrayList<NeighboringCellInfo> inputList = new ArrayList<>();
        final ArrayList retVal;

        cellListField.set(cellInfoListenerTest, new ArrayList<>());
        inputList.add(neighboringCellInfoMock);

        //noinspection deprecation
        Mockito.when(telephonyManager.getNeighboringCellInfo()).thenReturn(inputList);

        getNeighboringCellsInfoMethod.invoke(cellInfoListenerTest);
        retVal = (ArrayList) cellListField.get(cellInfoListenerTest);

        Assert.assertTrue(retVal.size() == 1);
    }
}
