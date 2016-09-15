package com.jpgrego.thesisapp.thesisapp.activities;

import android.telephony.TelephonyManager;
import android.widget.TableRow;
import com.jpgrego.thesisapp.thesisapp.BuildConfig;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import java.lang.reflect.Field;

/**
 * Created by jgrego on 15-09-2016.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 19)
public class MainActivityTest {

    private MainActivity mainActivityTest;
    private TelephonyManager telephonyManager;

    @Mock
    private TableRow tableRowMock;

    @Before
    public void setUp() throws NoSuchFieldException, IllegalAccessException {
        final Field telephonyManagerField;

        mainActivityTest = Robolectric.setupActivity(MainActivity.class);

        telephonyManagerField = MainActivity.class.getDeclaredField("telephonyManager");

        telephonyManagerField.setAccessible(true);

        telephonyManager = (TelephonyManager) telephonyManagerField.get(mainActivityTest);
    }

    @Test
    public void telephonyManagerNotNull() {
        Assert.assertNotNull(telephonyManager);
    }

    @Test
    public void cellInfoListenerNotNull() {
        Assert.assertNotNull(mainActivityTest.getCellInfoListener());
    }

    @Test
    public void wifiInfoReceiverNotNull() {
        Assert.assertNotNull(mainActivityTest.getWifiInfoReceiver());
    }


}
