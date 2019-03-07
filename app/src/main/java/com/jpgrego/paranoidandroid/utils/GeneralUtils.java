package com.jpgrego.paranoidandroid.utils;

/**
 * Created by jpgrego on 7/6/17.
 */

public final class GeneralUtils {

    // enforce noninstantiability
    private GeneralUtils() {
        throw new AssertionError();
    }

    public static String toHexString(final int num) {
        return Integer.toHexString(0x10000 | num).substring(1).toUpperCase();
    }
}
