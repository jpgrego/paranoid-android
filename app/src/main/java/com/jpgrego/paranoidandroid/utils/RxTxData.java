package com.jpgrego.paranoidandroid.utils;

public final class RxTxData {

    public final long rxBytes;
    public final long txBytes;

    RxTxData(final long rxBytes, final long txBytes) {
        this.rxBytes = rxBytes;
        this.txBytes = txBytes;
    }
}
