package com.jpgrego.paranoidandroid.utils;

public class RxTxData {

    public final long rxBytes;
    public final long txBytes;

    public RxTxData(final long rxBytes, final long txBytes) {
        this.rxBytes = rxBytes;
        this.txBytes = txBytes;
    }
}
