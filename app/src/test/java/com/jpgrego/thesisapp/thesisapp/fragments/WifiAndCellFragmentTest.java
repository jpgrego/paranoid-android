package com.jpgrego.thesisapp.thesisapp.fragments;

import android.widget.TableLayout;
import android.widget.TableRow;
import com.jpgrego.thesisapp.thesisapp.BuildConfig;
import com.jpgrego.thesisapp.thesisapp.activities.MainActivity;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowWifiManager;
import org.robolectric.shadows.support.v4.SupportFragmentTestUtil;

import java.lang.reflect.Field;

/**
 * Created by jpgrego on 14-09-2016.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(manifest = Config.NONE, constants = BuildConfig.class, sdk = 21, shadows = {ShadowWifiManager.class})
public class WifiAndCellFragmentTest {

    private WifiAndCellFragment wifiAndCellFragmentTest;
    private Field cellsTableField, wifiTableField;

    @Mock
    private TableRow tableRowMock;

    @Before
    public void setUp() throws NoSuchFieldException {
        wifiAndCellFragmentTest = new WifiAndCellFragment();
        cellsTableField = WifiAndCellFragment.class.getDeclaredField("cellsTable");
        wifiTableField = WifiAndCellFragment.class.getDeclaredField("wifiTable");

        Robolectric.buildActivity(MainActivity.class).attach().create().start().resume().get();
        SupportFragmentTestUtil.startFragment(wifiAndCellFragmentTest);
        cellsTableField.setAccessible(true);
        wifiTableField.setAccessible(true);
    }

    @Test
    public void updateCellTable() throws IllegalAccessException {
        final TableLayout cellsTable;

        cellsTable = (TableLayout) cellsTableField.get(wifiAndCellFragmentTest);

        Assert.assertTrue(cellsTable != null);
    }
}
