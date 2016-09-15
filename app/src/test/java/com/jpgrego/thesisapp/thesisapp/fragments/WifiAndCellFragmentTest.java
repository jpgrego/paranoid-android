package com.jpgrego.thesisapp.thesisapp.fragments;

import android.widget.TableLayout;
import com.jpgrego.thesisapp.thesisapp.BuildConfig;
import com.jpgrego.thesisapp.thesisapp.activities.MainActivity;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.support.v4.SupportFragmentTestUtil;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by jpgrego on 15-09-2016.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 19)
public class WifiAndCellFragmentTest {

    private WifiAndCellFragment wifiAndCellFragmentTest;
    private Method updateCellTableMethod;
    private Field wifiTableField, cellsTableField, cellUpdateHandlerField, wifiUpdateHandlerField;

    @Before
    public void setUp() throws NoSuchMethodException, NoSuchFieldException {
        wifiAndCellFragmentTest = new WifiAndCellFragment();
        updateCellTableMethod = WifiAndCellFragment.class.getDeclaredMethod("updateCellTable");
        wifiTableField = WifiAndCellFragment.class.getDeclaredField("wifiTable");
        cellsTableField = WifiAndCellFragment.class.getDeclaredField("cellsTable");
        cellUpdateHandlerField = WifiAndCellFragment.class.getDeclaredField("cellUpdateHandler");
        wifiUpdateHandlerField = WifiAndCellFragment.class.getDeclaredField("wifiUpdateHandler");

        wifiTableField.setAccessible(true);
        cellsTableField.setAccessible(true);
        cellUpdateHandlerField.setAccessible(true);
        wifiUpdateHandlerField.setAccessible(true);
        updateCellTableMethod.setAccessible(true);
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
    public void updateCellTableTest() throws InvocationTargetException, IllegalAccessException {
        final TableLayout cellsTable;
        updateCellTableMethod.invoke(wifiAndCellFragmentTest);

        cellsTable = (TableLayout) cellsTableField.get(wifiAndCellFragmentTest);

        Assert.assertTrue(cellsTable.getChildCount() == 1);
    }

}
