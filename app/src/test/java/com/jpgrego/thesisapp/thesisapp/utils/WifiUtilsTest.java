package com.jpgrego.thesisapp.thesisapp.utils;

import com.jpgrego.thesisapp.thesisapp.utils.WifiUtils;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;


import java.util.Arrays;
import java.util.Collection;


/**
 * Created by jgrego on 05-09-2016.
 */
@RunWith(value = Parameterized.class)
public class WifiUtilsTest {

    @Parameter (value = 0)
    public int frequency;

    @Parameter (value = 1)
    public int expResult;

    @Parameters(name = "{index}: frequencyToChannel({0}) = {1}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {2407, -1},

                {2412, 1},
                {2417, 2},
                {2422, 3},
                {2427, 4},
                {2432, 5},
                {2437, 6},
                {2442, 7},
                {2447, 8},
                {2452, 9},
                {2457, 10},
                {2462, 11},
                {2467, 12},
                {2472, 13},
                {2484, 14},

                {4915, 183},
                {4920, 184},
                {4925, 185},
                {4935, 187},
                {4940, 188},
                {4945, 189},
                {4960, 192},
                {4980, 196},

                {5035, 7},
                {5040, 8},
                {5045, 9},
                {5055, 11},
                {5060, 12},
                {5080, 16},

                {5170, 34},
                {5180, 36},
                {5190, 38},
                {5200, 40},
                {5210, 42},
                {5220, 44},
                {5230, 46},
                {5240, 48},
                {5250, 50},
                {5260, 52},
                {5270, 54},
                {5280, 56},
                {5290, 58},
                {5300, 60},
                {5310, 62},
                {5320, 64},

                {5500, 100},
                {5510, 102},
                {5520, 104},
                {5530, 106},
                {5540, 108},
                {5550, 110},
                {5560, 112},
                {5570, 114},
                {5580, 116},
                {5590, 118},
                {5600, 120},
                {5610, 122},
                {5620, 124},
                {5630, 126},
                {5640, 128},
                {5660, 132},
                {5670, 134},
                {5680, 136},
                {5690, 138},
                {5700, 140},
                {5710, 142},
                {5720, 144},
                {5745, 149},
                {5755, 151},
                {5765, 153},
                {5775, 155},
                {5785, 157},
                {5795, 159},
                {5805, 161},
                {5825, 165},

                {5835, -1}
        });
    }

    @Test
    public void frequencyToChannel() {
        Assert.assertEquals(expResult, WifiUtils.frequencyToChannel(frequency));
    }

}
