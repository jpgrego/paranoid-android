package com.jpgrego.paranoidandroid.listeners;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Created by jgrego on 20-09-2016.
 */
@RunWith(MockitoJUnitRunner.class)
public class SensorInfoListenerTest {

    private SensorInfoListener sensorInfoListenerTest;

    @Before
    public void setUp() {
        //sensorInfoListenerTest = new SensorInfoListener();
    }

    @Test
    public void sensorMapNotNull() {
        Assert.assertNotNull(sensorInfoListenerTest.getSensorList());
    }

}
