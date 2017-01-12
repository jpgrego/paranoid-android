package com.jpgrego.thesisapp.thesisapp.activities;

import android.widget.TableRow;
import com.jpgrego.thesisapp.thesisapp.BuildConfig;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import java.lang.reflect.Field;

/**
 * Created by jgrego on 15-09-2016.
 */
//@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 19)
public class MainActivityTest {

    private MainActivity mainActivityTest;

    @Mock
    private TableRow tableRowMock;

    @Before
    public void setUp() throws NoSuchFieldException, IllegalAccessException {
        final Field telephonyManagerField, sensorManagerField;

        mainActivityTest = Robolectric.setupActivity(MainActivity.class);

        telephonyManagerField = MainActivity.class.getDeclaredField("telephonyManager");
        sensorManagerField = MainActivity.class.getDeclaredField("sensorManager");

        telephonyManagerField.setAccessible(true);
        sensorManagerField.setAccessible(true);
    }

}
