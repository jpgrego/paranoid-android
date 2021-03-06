package com.jpgrego.paranoidandroid.activities;

import android.widget.TableRow;
import com.jpgrego.paranoidandroid.BuildConfig;
import org.junit.Before;
import org.mockito.Mock;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;

import java.lang.reflect.Field;

/**
 * Created by jgrego on 15-09-2016.
 */
//@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 19)
public class MainActivityTest {

    private WifiCellsBTActivity mainActivityTest;

    @Mock
    private TableRow tableRowMock;

    @Before
    public void setUp() throws NoSuchFieldException, IllegalAccessException {
        final Field telephonyManagerField, sensorManagerField;

        mainActivityTest = Robolectric.setupActivity(WifiCellsBTActivity.class);

        telephonyManagerField = WifiCellsBTActivity.class.getDeclaredField("telephonyManager");
        sensorManagerField = WifiCellsBTActivity.class.getDeclaredField("sensorManager");

        telephonyManagerField.setAccessible(true);
        sensorManagerField.setAccessible(true);
    }

}
