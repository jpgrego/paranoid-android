package com.jpgrego.thesisapp.thesisapp.data;

import android.support.annotation.NonNull;
import android.telephony.CellIdentityGsm;
import android.telephony.CellInfoGsm;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.NeighboringCellInfo;
import android.telephony.gsm.GsmCellLocation;

/**
 * Created by jpgrego on 25-08-2016.
 */

/*
 * No real point in implementing equals, since CellID is not always set
 */
public class Cell implements Comparable<Cell> {

    public final int mcc, mnc, cid, lac, psc, dbm;
    public final String generation;
    private boolean isRegisteredCell = false;

    public Cell(final int networkType, @NonNull final CellInfoGsm cellInfoGsm) {
        final CellIdentityGsm cellIdentityGsm;
        final CellSignalStrengthGsm cellSignalStrengthGsm;

        cellIdentityGsm = cellInfoGsm.getCellIdentity();
        cellSignalStrengthGsm = cellInfoGsm.getCellSignalStrength();

        this.mcc = cellIdentityGsm.getMcc();
        this.mnc = cellIdentityGsm.getMnc();
        this.cid = cellIdentityGsm.getCid();
        this.lac = cellIdentityGsm.getLac();
        //noinspection deprecation
        this.psc = cellIdentityGsm.getPsc();
        this.dbm = cellSignalStrengthGsm.getDbm();
        this.generation = CellUtils.getGenerationFromNetworkType(networkType);

        this.isRegisteredCell = cellInfoGsm.isRegistered();
    }

    public Cell(final int networkType, final int mcc, final int mnc, final int dbm,
                @NonNull final GsmCellLocation cellLocation) {
        this.mcc = mcc;
        this.mnc = mnc;
        this.cid = cellLocation.getCid();
        this.lac = cellLocation.getLac();
        this.psc = cellLocation.getPsc();
        this.dbm = dbm;
        this.generation = CellUtils.getGenerationFromNetworkType(networkType);
    }

    public Cell(final int networkType, final int mcc, final int mnc,
                @NonNull final NeighboringCellInfo neighboringCellInfo) {
        this.mcc = mcc;
        this.mnc = mnc;
        this.cid = neighboringCellInfo.getCid();
        this.lac = neighboringCellInfo.getLac();
        this.psc = neighboringCellInfo.getPsc();
        this.dbm = CellUtils.rssiToDbm(networkType, neighboringCellInfo.getRssi());
        this.generation = CellUtils.getGenerationFromNetworkType(networkType);
    }

    public void setRegisteredCell() {
        isRegisteredCell = true;
    }

    public boolean isRegisteredCell() {
        return isRegisteredCell;
    }

    @Override
    public int compareTo(@NonNull Cell another) {
        return another.dbm - this.dbm;
    }

}
