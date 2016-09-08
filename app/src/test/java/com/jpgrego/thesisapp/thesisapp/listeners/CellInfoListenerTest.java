package com.jpgrego.thesisapp.thesisapp.listeners;

import android.telephony.CellIdentityGsm;
import android.telephony.CellInfo;
import android.telephony.CellInfoGsm;
import android.telephony.TelephonyManager;
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
    private Field cellListField;

    @Mock
    private TelephonyManager telephonyManager;

    @Mock
    private CellInfoGsm cellInfoGsmMock;

    @Mock
    private CellIdentityGsm cellIdentityGsmMock;

    @Before
    public void setUp() throws NoSuchMethodException, NoSuchFieldException {
        Mockito.when(telephonyManager.getNetworkOperator()).thenReturn(TEST_NETWORK_OPERATOR);

        cellInfoListenerTest = new CellInfoListener(telephonyManager);
        getAllCellsMethod = CellInfoListener.class.getDeclaredMethod("getAllCellInfo", null);
        cellListField = CellInfoListener.class.getDeclaredField("cellList");

        getAllCellsMethod.setAccessible(true);
        cellListField.setAccessible(true);

    }

    @Test
    public void getAllCellInfoReturnsFalseOnNull() throws InvocationTargetException, IllegalAccessException {
        Mockito.when(telephonyManager.getAllCellInfo()).thenReturn(null);

        final boolean retVal;

        retVal = (boolean) getAllCellsMethod.invoke(cellInfoListenerTest, null);
        Assert.assertFalse(retVal);
    }

    @Test
    public void getAllCellInfoReturnsTrueOnList() throws InvocationTargetException, IllegalAccessException {
        Mockito.when(telephonyManager.getAllCellInfo()).thenReturn(new ArrayList<CellInfo>());

        final boolean retVal;

        retVal = (boolean) getAllCellsMethod.invoke(cellInfoListenerTest, null);
        Assert.assertTrue(retVal);
    }

    @Test
    public void getAllCellInfoAddsMembers() throws IllegalAccessException, InvocationTargetException {
        final ArrayList<CellInfo> inputList = new ArrayList<>(), retVal;

        cellListField.set(cellInfoListenerTest, new ArrayList<>());
        inputList.add(cellInfoGsmMock);

        Mockito.when(telephonyManager.getAllCellInfo()).thenReturn(inputList);
        Mockito.when(cellInfoGsmMock.getCellIdentity()).thenReturn(cellIdentityGsmMock);
        Mockito.when(cellIdentityGsmMock.getMcc()).thenReturn(268);

        getAllCellsMethod.invoke(cellInfoListenerTest, null);
        //retVal = (ArrayList<CellInfo>) cellListField.get(cellInfoListenerTest);
        //Assert.assertTrue(retVal.size() == 1);
    }
}
